<template>
  <div v-highlight v-html="htmlContent"></div>
</template>

<script>
import marked from 'marked'

export default {
  name: 'MarkdownBlock',
  data() {
    return {
      htmlContent: marked(
        this.mdContent.replace('{{JAKGD_BACKEND_URL}}',
          this.globalData.backendUrl,
        ), {sanitize: false}),
    }
  },
  props: {
    mdContent: String,
  },
  mounted() {
    // 重新加载latex语法
    if (window.MathJax.typesetPromise) {
      window.MathJax.typesetPromise()
    }
  },
  updated() {
    // 重新加载latex语法
    window.MathJax.typesetPromise()
  },
}
</script>

<style scoped>

</style>