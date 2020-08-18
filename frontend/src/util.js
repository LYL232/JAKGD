export default {
  /**
   * 获取简单的节点描述标题
   * @param node 节点对象
   * @returns {string} 标题
   */
  getNodeTitle(node) {
    let name = node.properties.name
    if (!name) {
      name = node.properties['名字']
    }
    return '(' + node.id + ')' + (name ? name : '')
  },
  /**
   * 默认错误提示
   * @param err 错误对象
   * @param hint 出错场景提示
   */
  errorHint(err, hint) {
    if (err.errorMsg) {
      this.$notify.error({
        title: '请求错误',
        message: hint + ': ' + err.errorMsg,
      })
    } else {
      this.$notify.error({
        title: '请求错误',
        message: hint + ': 未知错误',
      })
    }
  },
  /**
   * 生成[a, b)之间随机数
   * @param a
   * @param b
   * @returns {*}
   */
  randomNumInRangeWithA(a, b) {
    return a + Math.floor(Math.random() * (b - a)) //舍去
  }
}