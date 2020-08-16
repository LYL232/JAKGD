// highlight.js  代码高亮指令
import hljs from 'highlight.js'
import 'highlight.js/styles/googlecode.css'

let Highlight = {}
// 自定义插件
Highlight.install = function(Vue) {
  // 自定义指令 v-highlight
  Vue.directive('highlight', {
    // 被绑定元素插入父节点时调用
    inserted: (el) => {
      el.querySelectorAll('pre code').forEach((block) => {
        hljs.highlightBlock(block)
      })
    },
    // 指令所在组件的 VNode 及其子 VNode 全部更新后调用
    componentUpdated: (el) => {
      el.querySelectorAll('pre code').forEach((block) => {
        hljs.highlightBlock(block)
      })
    }
  })
}

export default Highlight