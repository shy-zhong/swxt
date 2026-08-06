

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'


const routes: RouteRecordRaw[] = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../components/common/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../components/common/Register.vue')
    },
    {
        path: '/user/home',
        name: 'UserHome',
        component: () => import('../components/user/UserHome.vue')
    },
    {
        path: '/user/shop',
        name: 'Shop',
        component: () => import('../components/user/Shop.vue')
    },
    {
        path: '/user/cart',
        name: 'Cart',
        component: () => import('../components/user/Cart.vue')
    },
    {
        path: '/operator/home',
        name: 'OperatorHome',
        component: () => import('../components/operator/OperatorHome.vue')
    },
    {
        path: '/operator/stockManager',
        name: 'StockManager',
        component: () => import('../components/operator/StockManager.vue')
    },
    {
        path: '/operator/orders',
        name: 'OrderManagement',
        component: () => import('../components/operator/OrderManagement.vue')
    },
    {
        path: '/operator/stockLog',
        name: 'StockLog',
        component: () => import('../components/operator/StockLog.vue')
    },
    {
        path: '/operator/settings',
        name: 'OperatorSettings',
        component: () => import('../components/operator/OperatorSettings.vue')
    },
    {
        path: '/admin/home',
        name: 'AdminHome',
        component: () => import('../components/admin/AdminHome.vue')
    },
    {
        path: '/admin/users',
        name: 'UserManagement',
        component: () => import('../components/admin/UserManagement.vue')
    },
    {
        path:'/admin/products',
        name:'ProductManager',
        component:()=>import('../components/admin/ProductManager.vue')
    },
    {
        path:'/admin/statistics',
        name:'Statistics',
        component:()=>import('../components/admin/Statistics.vue')
    },
    {
        path:'/admin/settings',
        name:'SystemSettings',
        component:()=>import('../components/admin/SystemSettings.vue')
    },
    {
        path:'/admin/logs',
        name:'SystemLogs',
        component:()=>import('../components/admin/SystemLogs.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

/**
 * 解析 JWT 的 exp 字段，判断令牌是否过期
 */
function isTokenExpired(token: string): boolean {
    try {
        const payload = token.split('.')[1]
        const decoded = JSON.parse(atob(payload))
        if (!decoded.exp) return false
        return Date.now() / 1000 >= decoded.exp
    } catch {
        return false
    }
}

/**
 * 按角色返回对应的首页路径：ADMIN 进管理后台，OPERATOR 进操作员首页，USER 进用户主页
 */
function getHomeByRole(): string {
    const role = localStorage.getItem('role')
    if (role === 'ADMIN') return '/admin/home'
    if (role === 'OPERATOR') return '/operator/home'
    return '/user/home'
}


router.beforeEach((to) => {
    const token = localStorage.getItem('token')

    if (to.path === '/login' || to.path === '/register') {
        if (token && !isTokenExpired(token)) {
            return getHomeByRole()
        }
        return
    }

    if (!token || isTokenExpired(token)) {
        localStorage.removeItem('token')
        localStorage.removeItem('role')
        localStorage.removeItem('username')
        return '/login'
    }

    const role = localStorage.getItem('role')
    if (to.path.startsWith('/admin') && role !== 'ADMIN') {
        return getHomeByRole()
    }
    if (to.path.startsWith('/operator') && role !== 'OPERATOR') {
        return getHomeByRole()
    }
    if (to.path.startsWith('/user') && role !== 'USER') {
        return getHomeByRole()
    }
})

export default router
