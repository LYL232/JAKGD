<template>
  <v-md-preview :text="content" class="md-div"/>
</template>

<script>
import './md-common.css'

export default {
  name: 'MarkdownBlock',
  computed: {
    content() {
      return this.mdContent.replace('{{JAKGD_BACKEND_URL}}', window.location.origin)
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