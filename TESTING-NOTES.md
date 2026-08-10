# swxt 浏览器自动化测试流程与踩坑记录

记录一次真实完成的端到端测试(野图片回滚验证)的完整流程与踩坑经验,避免下次重复踩坑。
环境:Windows + PowerShell + Microsoft Edge 调试实例 + browser-harness(CDP)。

## 一、前置条件

1. 前后端已启动:前端 `http://localhost:5173`,后端 `http://localhost:8887/api`。
2. 准备测试图片(1x1 像素 PNG 即可),放独立临时目录,测试后删除。

## 二、启动 Edge 调试实例(必须后台保活!)

```powershell
Start-Process "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" -ArgumentList "--remote-debugging-port=9222","--user-data-dir=D:\Programs\browser-harness\edge-profile-2","--no-first-run","--no-default-browser-check","about:blank"; while($true){ Start-Sleep 60 }
```

- **必须**用 bash 工具的 `run_in_background=true` 运行(或等价保活)。
- ⚠️ **坑 A:Edge 会被 bash 命令结束杀掉**。普通前台命令结束后,进程组被清理,9222 端口随之消失,表现为"之前还通,后来全部 10061"。保活循环(`while($true){ Start-Sleep 60 }`)可避免。测试完用 `kill_shell` 停掉。

## 三、验证端口(别信 Invoke-WebRequest!)

- ⚠️ **坑 B:PowerShell 的 `Invoke-WebRequest` 会走系统代理,把不存在的端口伪装成 200**。本机实际发生过:9222 根本没监听,`Invoke-WebRequest http://127.0.0.1:9222/json/version` 却返回 200(代理响应),误导排查。
- 正确验证方式:
  ```powershell
  netstat -ano | Select-String ":9222"   # 看 LISTENING 行
  ```
  或 Python(不走代理):
  ```powershell
  python -c "import urllib.request; print(urllib.request.urlopen('http://127.0.0.1:9222/json/version', timeout=3).status)"
  ```

## 四、执行 browser-harness 脚本(编码是最大的坑!)

### 调用方式(必须用 cmd 重定向,且脚本无 BOM)

```powershell
$py = @'
# 脚本内容,见下
'@
[IO.File]::WriteAllText("D:\path\script.py", $py, (New-Object System.Text.UTF8Encoding($false)))
cmd /c "set ""BU_CDP_URL=http://127.0.0.1:9222"" && D:\Programs\uv\bin\browser-harness.exe < D:\path\script.py" 2>&1 | Out-String
```

### ⚠️ 坑 C:脚本内任何地方都不能出现中文字符(包括注释!)

中文经 PowerShell 管道传给 Python 时会被损坏成 UTF-16 surrogate,报错:
`UnicodeEncodeError: 'utf-8' codec can't encode character '\udca2' in position ...: surrogates not allowed`
- 位置(如 position 22、position 659)指向脚本里**第一处中文**(注释也算!)。
- 解决办法:**全 ASCII**,中文字符串用 `\uXXXX` 转义写在 Python 字符串里(Python 自己解码),例如:
  - 新增商品 `\u65b0\u589e\u5546\u54c1`
  - 确认新增 `\u786e\u8ba4\u65b0\u589e`
  - 取消 `\u53d6\u6d88`
  - 编辑 `\u7f16\u8f91`
  - 查询 `\u67e5\u8be2`
  - 保存 `\u4fdd\u5b58`
- 注释一律写英文。

## 五、常用操作模板

### 1. 登录(用 input[type] 定位,避免中文 placeholder)

```python
js("document.querySelector('input[type=text]').focus()")
cdp("Input.insertText", text="admin")
js("document.querySelector('input[type=password]').focus()")
cdp("Input.insertText", text="123456")
# 点击唯一按钮(登录)
nodes = cdp("Accessibility.getFullAXTree")["nodes"]
btns = [n for n in nodes if n.get("role", {}).get("value") == "button"]
q = cdp("DOM.getBoxModel", backendNodeId=btns[0]["backendDOMNodeId"])["model"]["content"]
click_at_xy(sum(q[0::2])/4, sum(q[1::2])/4)
```

### 2. 点击指定文本按钮(用 \u 转义)

```python
js("Array.from(document.querySelectorAll('button')).find(b=>b.textContent.trim()==='\u65b0\u589e\u5546\u54c1').click()")
```

### 3. 上传文件(关键:nodeId 每次调用内重新取!)

- ⚠️ **坑 D:CDP nodeId 跨 browser-harness 调用失效**。每次调用是独立 CDP 会话,前一次拿到的 nodeId 再用时报 `Could not find node with given id`。必须**同一次调用内**重新查询:

```python
doc = cdp("DOM.getDocument")["root"]["nodeId"]
q = cdp("DOM.querySelector", nodeId=doc, selector="input[type=file]")
cdp("DOM.setFileInputFiles", nodeId=q["nodeId"], files=[r"D:\path\test1.png"])
```

### 4. 填表单(v-model 需要触发 input 事件)

```python
js("Array.from(document.querySelectorAll('.panel-overlay input[type=text]'))[0].value='x'.repeat(300); Array.from(document.querySelectorAll('.panel-overlay input[type=text]'))[0].dispatchEvent(new Event('input',{bubbles:true}))")
```

### 5. 制造"创建失败"(本项目后端无业务校验,用 DB 约束触发)

- `product.name` 是 `varchar(200) NOT NULL`,填 300 字符 → MySQL `Data too long` → `GlobalExceptionHandler` 返回 400"数据校验失败,请检查输入内容" → 前端走失败分支。

## 六、验证"无野图片"的方法

1. **测试前记录基线**:`(Get-ChildItem "D:\project\springboot+vue\swxt\uploads\product").Count` 及文件名列表。
2. 每个场景:上传 → 触发(取消/失败)→ 再查目录,确认回到基线。
3. 三种路径都验证:
   - 上传后**取消创建**:`cancelCreate` → `rollbackImage` 删图
   - 上传后**创建失败**(超长 name):失败分支 `rollbackImage` 删图,**弹窗保持打开**
   - 编辑**换图后取消**:`cancelEdit` 判断 `image !== originalEditImage` 才删新图,旧图保留
4. 测试后清理:删测试图片/脚本/临时目录,确认 `uploads/product` 回到基线;`kill_shell` 停掉 Edge 保活 job。

## 七、⚠️ 坑 E:测试脚本间的状态污染(曾造成假象)

连续多次跑不同测试脚本时,**上一次残留的弹窗遮罩(overlay)会盖住页面**。此时点击"新增商品"等页面按钮,点击实际命中遮罩 → 触发 ModalPanel 的 `@click.self` → `emit('close')` → `cancelCreate`(关弹窗+删图+清空 error),看起来像"失败后弹窗关闭"的 bug,实则是测试污染。
- 应对:
  - 每个测试脚本开头先确认页面干净:`js("!!document.querySelector('.panel-overlay')")` 为 false 再继续;
  - 关键断言(如提交后弹窗状态、error、目录文件数)在**同一次 browser-harness 调用内连续监控**(如 `for i in range(6): sleep(0.8); print(overlay, error)`),不要跨调用对比;
  - 页面级 error 在 `.product-management .error`(不在弹窗内),检查时用对选择器。

## 八、其他备忘

- 登录状态存 localStorage,复用 `--user-data-dir` 目录可跨测试保留登录。
- 浏览器目标顺序不是标签页顺序;`goto_url` 前已连接时直接用,首次导航用 `new_tab(url)`。
- 测试账号:admin / 123456(DataInitializer 自动创建)。
