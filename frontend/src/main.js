import {createApp} from 'vue'
import axios from 'axios'
import VueAxios from 'vue-axios'
import App from './App.vue'
import ElementPlus from 'element-plus'
// import MavonEditor from 'mavon-editor'
import bus from './bus'

// 解决chrome浏览器警告, 如果加上, d3中的组件会疯狂报错 TODO: 解决
// import 'default-passive-events'
import 'element-plus/dist/index.css'

// import 'mavon-editor/dist/css/index.css'
import globalData from './global-data'
import util from './util'
import router from './router'

// MARKDOWN EDITOR BEGIN
import VMdEditor from '@kangc/v-md-editor'
import VMdPreview from '@kangc/v-md-editor/lib/preview'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import '@kangc/v-md-editor/lib/style/preview.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'
import hljs from 'highlight.js'
// MARKDOWN EDITOR END

const app = createApp(App)

app.use(router)

VMdEditor.use(githubTheme, {Hljs: hljs})
VMdPreview.use(githubTheme, {Hljs: hljs})
app.use(VMdEditor)
app.use(VMdPreview)

// 事件传输总线
app.config.globalProperties.bus = bus
// 全局数据
app.config.globalProperties.globalData = globalData
// 全局实用函数
app.config.globalProperties.util = util

axios.defaults.baseURL = 'http://backend:8080'
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

app.use(ElementPlus)
app.use(VueAxios, axios)

util.$notify = app.config.globalProperties.$notify

app.mount('#app')
