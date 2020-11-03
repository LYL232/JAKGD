import Vue from 'vue'
import axios from 'axios'
import VueAxios from 'vue-axios'
import App from './App.vue'
import ElementUI from 'element-ui'
import MavonEditor from 'mavon-editor'

// 解决chrome浏览器警告, 如果加上, d3中的组件会疯狂报错 TODO: 解决
// import 'default-passive-events'
import 'element-ui/lib/theme-chalk/index.css'
import 'mavon-editor/dist/css/index.css'
import globalData from './global-data'
import util from './util'
import router from './router'

// 事件传输总线
Vue.prototype.bus = new Vue()
// 全局数据
Vue.prototype.globalData = globalData
// 全局实用函数
Vue.prototype.util = util

axios.defaults.baseURL = window.location.origin
// 请求拦截器
axios.interceptors.request.use(
  config => {
    if (globalData.user && globalData.user.authorization) {
      // 加入服务器认证请求头
      config.headers.Authorization = globalData.user.authorization
    }
    return config
  },
  err => {
    return Promise.reject(err)
  },
)

// 响应拦截器
axios.interceptors.response.use(
  response => {
    if (response.status >= 200 && response.status < 300) {
      return Promise.resolve(response)
    } else {
      return Promise.reject(response)
    }
  },
  error => {
    if (error.response) {
      if (error.response.data) {
        let data = error.response.data
        if (data.code) {
          error.errorCode = data.code
          error.errorMsg = globalData.responseCodeMessage[data.code]
            + ': ' + data.message
          if (data.code === 1) {
            if (globalData.user) {
              this.bus.$emit('authorizationChange', {
                username: null,
                authorization: null,
              })
            }
          }
        } else {
          console.log('no node response')
          console.log(error)
          error.errorMsg = '未知错误: 无法解析错误'
        }
      } else {
        console.log('no response error')
        console.log(error)
        error.errorMsg = '未知错误: 无响应'
      }
    } else {
      console.log('unexpected error')
      console.log(error)
      error.errorMsg = '未知错误: 无响应'
    }
    return Promise.reject(error)
  },
)

Vue.config.productionTip = false
Vue.use(MavonEditor)
Vue.use(ElementUI)
// Vue.use(hl)
Vue.use(VueAxios, axios)

util.$notify = Vue.prototype.$notify

// noinspection JSUnusedGlobalSymbols
new Vue({
  render: h => h(App),
  router,
}).$mount('#app')
