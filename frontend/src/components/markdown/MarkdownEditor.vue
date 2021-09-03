<template>
  <div>
    <h1 v-if="header">{{ header + (docId ? '(' + docId + ')' : '') }}</h1>
    <el-divider></el-divider>
    <v-md-editor class="md-div" v-model="content" ref="editor" @save="save"
                 :left-toolbar="leftToolbar" :disabled-menus="disabledMenus"
                 @upload-image="handleUploadImage" @change="handleChange"
    />
  </div>
</template>

<script>
import './md-common.css'

const originRegExp = new RegExp(window.location.origin, 'g'),
    backEndRegExp = new RegExp('\{\{JAKGD_BACKEND_URL\}\}', 'g')

export default {
  name: 'MarkdownEditor',
  props: {
    doc: {
      type: Object,
      default: null,
    },
    node: {
      type: Object,
      default: null,
    },
    header: String,
  },
  data() {
    return {
      docId: null,
      content: this.doc.content.replace(backEndRegExp, window.location.origin),
      leftToolbar: 'undo redo clear | h bold italic strikethrough quote | ul ol table hr | link image code | save',
      /*
        undo	撤销
        redo	重做
        clear	清空
        h	标题
        bold	粗体
        italic	斜体
        strikethrough	中划线
        quote	引用
        ul	无序列表
        ol	有序列表
        table	表格
        hr	分割线
        link	链接
        image	插入图片
        code	代码块
        save	保存，点击后触发save事件
      * */
      toolbarConfig: {},
      disabledMenus: [],
      // toolbars: {
      //   // TODO: 上传图片
      //   bold: true, // 粗体
      //   italic: true, // 斜体
      //   header: true, // 标题
      //   underline: true, // 下划线
      //   strikethrough: true, // 中划线
      //   mark: true, // 标记
      //   superscript: true, // 上角标
      //   subscript: true, // 下角标
      //   quote: true, // 引用
      //   ol: true, // 有序列表
      //   ul: true, // 无序列表
      //   link: true, // 链接
      //   imagelink: true, // 图片链接
      //   code: true, // code
      //   table: true, // 表格
      //   fullscreen: true, // 全屏编辑
      //   readmodel: true, // 沉浸式阅读
      //   htmlcode: true, // 展示html源码
      //   help: true, // 帮助
      //   /* 1.3.5 */
      //   undo: true, // 上一步
      //   redo: true, // 下一步
      //   trash: true, // 清空
      //   save: true, // 保存（触发events中的save事件）
      //   /* 1.4.2 */
      //   navigation: true, // 导航目录
      //   /* 2.1.8 */
      //   alignleft: true, // 左对齐
      //   aligncenter: true, // 居中
      //   alignright: true, // 右对齐
      //   /* 2.2.1 */
      //   subfield: true, // 单双栏模式
      //   preview: true, // 预览
      // },
    }
  },
  mounted() {
    if (this.doc.id) {
      this.docId = this.doc.id
    }
    setTimeout(() => {
      window.MathJax.typesetPromise()
    }, 500)
  },
  methods: {
    save(text, html) {
      let doc = this.doc
      if (!doc.name || doc.name === '') {
        this.$notify.info({
          title: '输入错误',
          message: '文档名不能为空',
        })
        return
      }

      if (!this.node) {
        this.$notify.error({
          title: '未知错误',
          message: '文档所属节点未知, 请关闭当前页面',
        })
        return
      }

      text = text.replace(originRegExp, '{{JAKGD_BACKEND_URL}}')

      if (this.docId) {
        this.axios.put('/api/document/' + this.docId, {content: text}).then(() => {
          this.$notify({
            title: '成功',
            type: 'success',
            message: '保存成功',
          })
        }).catch(err => {
          this.util.errorHint(err, '保存失败')
        })
      } else {
        this.axios.post('/api/document/markdown/' + doc.name +
            '/partOf/' + this.node.id, {content: text}).then(response => {
          this.docId = response.data
          this.$notify({
            title: '成功',
            type: 'success',
            message: '创建文档成功',
          })
        }).catch(err => {
          this.util.errorHint(err, '创建文档失败')
        })
      }
    },
    handleUploadImage(event, insertImage, files) {
      // 拿到 files 之后上传到文件服务器，然后向编辑框中插入对应的内容
      files.forEach(file => {
        let param = new FormData()
        param.append('file', file)
        this.axios.post('/api/file', param, {
          headers: {'Content-Type': 'multipart/form-data'},
        }).then(res => {
          insertImage({
            url: window.location.origin + '/media' + res.data,
            desc: file.name,
            width: 'auto',
            height: 'auto',
          })
          this.$notify({
            title: '上传成功',
            type: 'success',
            message: '文件上传成功',
          })
        }).catch(err => {
          this.util.errorHint(err, '图片' + file.name + '上传失败')
        })
      })
    },
    handleChange() {
      setTimeout(() => {
        window.MathJax.typesetPromise()
      }, 250)
    }
  },
}
</script>

<style scoped>

</style>