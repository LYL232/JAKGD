<template>
  <div class='content' v-highlight v-html='htmlContent'></div>
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
        ), {
          'baseUrl': null,
          'breaks': false,
          'gfm': true,
          'headerIds': true,
          'headerPrefix': '',
          'highlight': null,
          'langPrefix': 'language-',
          'mangle': true,
          'pedantic': false,
          'sanitize': false,
          'sanitizer': null,
          'silent': false,
          'smartLists': false,
          'smartypants': false,
          'tokenizer': null,
          'walkTokens': null,
          'xhtml': false
        }),
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
  .content {
    white-space: pre-wrap;
    word-break: break-all;
  }
</style>