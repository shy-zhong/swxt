import {reactive} from 'vue'
import {get} from './request'


interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  description: string
}


const configStore = reactive<{
  loaded: boolean
  configs: Record<string, string>
}>({
  loaded: false,
  configs: {}
})

/**
 * 加载系统配置：请求公共接口，填充 configs 映射并设置页面标题
 */
export async function loadConfig(): Promise<void> {
  try {
    const res = await get<SystemConfig[]>('/system-config/public')
    if (res.success && res.data) {
      for (const item of res.data) {
        configStore.configs[item.configKey] = item.configValue
      }
      configStore.loaded = true

      const siteName = configStore.configs['site_name']
      if (siteName) {
        document.title = siteName
      }
    }
  } catch (e) {
    console.error('加载系统配置失败', e)
  }
}

/**
 * 获取指定配置项的值
 */
export function getConfig(key: string): string {
  return configStore.configs[key] ?? ''
}

export { configStore }
