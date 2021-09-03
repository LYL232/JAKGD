<template>
  <v-md-preview :text="content" class="md-div"/>
</template>

<script>
import './md-common.css'
const backEndRegExp = new RegExp('\{\{JAKGD_BACKEND_URL\}\}', 'g')

export default {
  name: 'MarkdownBlock',
  computed: {
    content() {
      return this.mdContent.replace(backEndRegExp, window.location.origin)
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