import Vue from 'vue'
import VueRouter from 'vue-router'
import NotFound from './components/NotFound'

// 引入组件
const Home = () => import('./components/home/Home'),
  Me = () => import('./components/me/Me')

// 要告诉 vue 使用 vueRouter
Vue.use(VueRouter)

const routes = [
  {
    path: '/home',
    component: Home
  },
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/me',
    component: Me
  },
  {
    path: '*',
    component: NotFound
  }
]

const router = new VueRouter({
  routes, mode: 'history'
})
export default router

