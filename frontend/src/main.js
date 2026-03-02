import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/theme.css'

// 每次重新进入站点都要求重新登录，避免沿用历史会话
localStorage.removeItem('token')
localStorage.removeItem('userInfo')

createApp(App).use(router).use(ElementPlus).mount('#app')
