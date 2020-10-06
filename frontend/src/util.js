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
   * @param a {number}
   * @param b {number}
   * @returns {number}
   */
  randomNumInRange(a, b) {
    return a + Math.floor(Math.random() * (b - a)) //舍去
  },

  /**
   * 16进制颜色字符串转RGB
   * @param color {string}
   * @returns {{r:number, g: number, b: number}}
   */
  string2RGB(color) {
    let r, g, b
    if (color.length === 4) {//4位颜色处理,简写模式
      r = parseInt(color.substring(1, 2) + color.substring(1, 2), 16)
      g = parseInt(color.substring(2, 3) + color.substring(2, 3), 16)
      b = parseInt(color.substring(3) + color.substring(3), 16)
    } else {//7位颜色字符串处理
      r = parseInt(color.substring(1, 3), 16)
      g = parseInt(color.substring(3, 5), 16)
      b = parseInt(color.substring(5), 16)
    }
    return {r, g, b}
  },

  /**
   * 255RGB转16进制颜色字符串
   * @param r {number} 0-255红色分量
   * @param g {number} 0-255绿色分量
   * @param b {number} 0-255蓝色分量
   * @returns {string} 16进制颜色字符串
   */
  RGB2String({r, g, b}) {
    return '#' + r.toString(16) + g.toString(16) + b.toString(16)
  },
}