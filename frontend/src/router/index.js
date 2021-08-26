import {createRouter, createWebHistory} from 'vue-router'

// 引入组件
const NotFound = () => import('../components/NotFound.vue'),
  Home = () => import('../components/home/Home.vue'),
  Me = () => import('../components/me/Me.vue')


const router = createRouter({
  routes: [
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
      path: '/:pathMatch(.*)*',
      name: '404 Not Found',
      component: NotFound
    }
  ],
  history: createWebHistory(),
})

export default router
