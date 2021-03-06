<template>
  <div>
    <h1 v-if="header">{{header + (docId ? '(' + docId + ')' : '')}}</h1>
    <el-divider></el-divider>
    <mavon-editor style="min-height: 600px" v-model="content" ref="editor" @save="save" :ishljs="true"
                  :toolbars="toolbars" @imgAdd="imgAdd" @imgDel="imgDel"/>
  </div>
</template>

<script>

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
      content: this.doc.content.replace('{{JAKGD_BACKEND_URL}}', window.location.origin),
      toolbars: {
        // TODO: 上传图片
        bold: true, // 粗体
        italic: true, // 斜体
        header: true, // 标题
        underline: true, // 下划线
        strikethrough: true, // 中划线
        mark: true, // 标记
        superscript: true, // 上角标
        subscript: true, // 下角标
        quote: true, // 引用
        ol: true, // 有序列表
        ul: true, // 无序列表
        link: true, // 链接
        imagelink: true, // 图片链接
        code: true, // code
        table: true, // 表格
        fullscreen: true, // 全屏编辑
        readmodel: true, // 沉浸式阅读
        htmlcode: true, // 展示html源码
        help: true, // 帮助
        /* 1.3.5 */
        undo: true, // 上一步
        redo: true, // 下一步
        trash: true, // 清空
        save: true, // 保存（触发events中的save事件）
        /* 1.4.2 */
        navigation: true, // 导航目录
        /* 2.1.8 */
        alignleft: true, // 左对齐
        aligncenter: true, // 居中
        alignright: true, // 右对齐
        /* 2.2.1 */
        subfield: true, // 单双栏模式
        preview: true, // 预览
      },
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
    save(value) {
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

      value = value.replace(window.location.origin, '{{JAKGD_BACKEND_URL}}')

      if (this.docId) {
        this.axios.put('/api/document/' + this.docId, {content: value}).then(() => {
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
          '/partOf/' + this.node.id, {content: value}).then(response => {
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
    imgAdd(pos, file) {
      let param = new FormData()
      param.append('file', file)
      this.axios.post('/api/file', param, {
        headers: {'Content-Type': 'multipart/form-data'},
      }).then(res => {
        this.$refs.editor.$img2Url(pos, window.location.origin + '/media' + res.data)
      }).catch(err => {
        delete this.$refs.editor.$refs.toolbar_left.$imgDelByFilename(file.name)
        this.util.errorHint(err, '图片上传失败')
      })
    },
    imgDel(pos) {
      pos[0] = pos[0].toString()
      if (pos[0].startsWith(window.location.origin + '/media')) {
        let path = pos[0].replace(window.location.origin + '/media', '')
        this.axios.delete('/api/file', {
          params: {path},
        }).then(() => {
          this.$refs.editor.$refs.toolbar_left.$imgDelByFilename(pos[0])
        }).catch(err => {
          this.util.errorHint(err, '图片删除失败')
        })
      }
    },
  },
}
</script>

<style scoped>

</style>