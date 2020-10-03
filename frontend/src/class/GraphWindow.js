// 参考 neo4jd3 源码修改而来

import globalData from '../global-data'

const d3 = require('d3')

// 合并两个对象
function merge(target, source) {
  Object.keys(source).forEach(function(property) {
    target[property] = source[property]
  })
}

/**
 * 计算两个指定点方位角(弧度)函数
 * @param source {{x:number, y:number}}
 * @param target {{x:number, y:number}}
 * @returns {number} 方位角(弧度)
 * @private
 */
function getAngle(source, target) {
  return Math.atan2(target.y - source.y, target.x - source.x)
    * 180 / Math.PI
}

/**
 * 计算两个点的欧氏距离
 * @param t {{x: number, y: number}}
 * @param s {{x: number, y: number}}
 * @returns {number}
 */
function euclideanDistance(t, s) {
  let x = t.x - s.x, y = t.y - s.y
  return Math.sqrt(x * x + y * y)
}

/**
 * 统一化向量, TODO: 目前作用未知
 * @param source {{x: number, y: number}} 源点
 * @param target {{x: number, y: number}} 终点
 * @param newLength {Number} 新向量的长度
 * @returns {{x: number, y: number}}
 */
function unitaryVector(source, target, newLength) {
  // length小于等于欧氏距离
  let length = euclideanDistance(source, target) / Math.sqrt(newLength || 1)
  return {
    x: (target.x - source.x) / length,
    y: (target.y - source.y) / length,
  }
}

/**
 * 计算终点围绕起点旋转angle之后的点
 * @param cx {number} 起点x坐标
 * @param cy {number} 起点y坐标
 * @param x {number} 终点x坐标
 * @param y {number} 终点y坐标
 * @param angle {number} 旋转角度
 * @returns {{x: Number, y: Number}} 新的终点
 */
function rotate(cx, cy, x, y, angle) {
  let rad = (Math.PI / 180) * angle,
    cos = Math.cos(rad),
    sin = Math.sin(rad),
    nx = (cos * (x - cx)) + (sin * (y - cy)) + cx,
    ny = (cos * (y - cy)) - (sin * (x - cx)) + cy
  return {x: nx, y: ny}
}

/**
 * 包装rotate函数
 * @param c {{x: number, y: number}} 起点
 * @param p {{x: number, y: number}} 终点
 * @param angle {number} 角度
 * @returns {{x: number, y: number}}
 */
function rotatePoint(c, p, angle) {
  return rotate(c.x, c.y, p.x, p.y, angle)
}

/**
 * 统一化向量, TODO: 目前作用未知
 * @param source {{x: number, y: number}} 源点
 * @param target {{x: number, y: number}} 终点
 * @param newLength {number} 新向量的长度
 * @returns {{x: Number, y: Number}}
 */
function unitaryNormalVector(source, target, newLength) {
  let center = {x: 0, y: 0},
    vector = unitaryVector(source, target, newLength)
  return rotatePoint(center, vector, 90)
}

/**
 * 获得指定颜色的暗色
 * @param color 颜色字符串
 * @returns String
 */
function darkerColor(color) {
  // noinspection JSUnresolvedFunction
  return d3.rgb(color).darker(1)
}

// 内部属性集(不展示), 属性名字映射(内部表示=>外部表示), 标签名字映射
let internalProperties = globalData.internalProperties,
  propertyNameMap = globalData.propertyNameMap,
  nodeLabelNameMap = globalData.nodeLabelNameMap,
  relationshipTypeNameMap = globalData.relationshipTypeNameMap

/**
 * 用于图展示的node对象, 有x, y等展示时需要的属性, 而且会修改一些属性名,
 * 为了不改变原对象, 所以需要一个拓展类来表示
 */
class GraphNode {
  /**
   * 根据node创建对象
   * @param node {{id, labels, properties}} 有labels
   * @param x {number} 初始x坐标
   * @param y {number} 初始y坐标
   */
  constructor(node, x, y) {
    this.x = x
    this.y = y
    this.properties = {}
    this.id = node.id
    this.labels = []
    // 节点作为关系的源点的id的集合
    this.asRelSource = []
    // 节点作为关系的终点的id的集合
    this.asRelTarget = []

    let that = this
    // 删除不必要属性 属性名映射
    Object.keys(node.properties).forEach(function(property) {
      if (!internalProperties.has(property)) {
        that.properties[propertyNameMap(property)] = node.properties[property]
      }
    })
    node.labels.forEach((label) => {
      // 替换labels的名字(英文=>中文, 内部表示=>展示表示)
      that.labels.push(nodeLabelNameMap(label))
    })
  }

  toString() {
    let s = ' (<id>: ' + this.id

    let that = this
    Object.keys(this.properties).forEach(function(property) {
      s += ', ' + property + ': ' +
        JSON.stringify(that.properties[property])
    })

    s += ')'

    return s
  }
}

/**
 * 用于图展示的relationship对象, 有x, y, source, target等展示时需要的属性,
 * 而且会修改一些属性名, 为了不改变原对象, 所以需要一个拓展类来表示
 */
class GraphRelationship {
  /**
   * 根据relationship对象构造
   * @param relationship {{id:number, type: string, properties: Object}}
   * @param source {GraphNode} 起始点
   * @param target {GraphNode} 终止点
   */
  constructor(relationship, source, target) {
    this.id = relationship.id
    this.type = relationshipTypeNameMap(relationship.type)
    this.source = source
    this.target = target
    // 维护关系端点到其涉及到的关系的集合
    // 维护id集合, 因为直接push(this)的话this对应的关系对象在relationshipMap中被
    // 替换, asRelSource和asRelTarget里的对象将失效
    source.asRelSource.push(this.id)
    target.asRelTarget.push(this.id)
    this.properties = {}

    // 删除不必要属性 属性名映射
    let that = this
    Object.keys(relationship.properties).forEach(function(property) {
      if (!internalProperties.has(property)) {
        that.properties[
          propertyNameMap(property)] = relationship.properties[property]
      }
    })
  }
}

/**
 * 信息面板类
 */
class InfoPanel {
  // DOM 对象
  _panel

  /**
   * 构造函数
   * @param container dom容器
   */
  constructor(container) {
    this._panel = container.append('div').
      attr('class', 'graph-window-info')
  }

  /**
   * 清除信息面板
   */
  clear() {
    this._panel.html('')
  }

  /**
   * 增加信息项 '属性: 属性值'
   * @param cls {string} class css类别
   * @param property {string} 属性
   * @param value {string} 属性值
   * @param {{
   * backgroundColor: function, 背景颜色函数, 返回颜色字符串
   * borderColor: function, 边界颜色函数, 返回颜色字符串
   * color: function, 文字颜色函数, 返回颜色字符串
   * }}
   */
  appendElement(cls, property, value,
    {backgroundColor, borderColor, color}) {
    // noinspection CheckTagEmptyBody
    let elem = this._panel.append('a')
    // noinspection CheckTagEmptyBody
    elem.attr('href', '#').
      attr('class', cls).
      html('<strong>' + property + '</strong>' +
        (value ? (': ' + value) : ''))

    if (backgroundColor) {
      elem.style('background-color', backgroundColor)
    }
    if (borderColor) {
      elem.style('border-color', borderColor)
    }
    if (color) {
      elem.style('color', color)
    }
  }

  /**
   * 增加一个换行
   */
  appendBr() {
    this._panel.append('br')
  }

}

class GraphWindow {
  // 未知
  _svgTranslate = null

  /**
   * 使用选择器对应的DOM对象构造该类对象
   * @param selector {string} 选择器
   * @param optionsProvided {*} 选项
   */
  constructor(selector, optionsProvided) {

    // 记录承载着relationship dom对象的dom对象
    this._svgRelationships = null
    // 记录承载着node dom对象的dom对象
    this._svgNodes = null
    // 力导向图模拟对象
    this._simulation = null
    // 记录当前缩放倍数
    this._svgScale = null
    // 记录着运行时所有node的dom对象, class == node
    this._node = null
    // 记录着运行时所有relationship的dom对象, class == relationship
    this._relationship = null
    // 记录着运行时所有relationship text的dom对象, class == relationship & text
    this._relationshipText = null
    // 记录着运行时所有relationship overlay的dom对象, class == relationship & overlay
    this._relationshipOverlay = null
    // 记录着运行时所有relationship Outline的dom对象, class == relationship & outline
    this._relationshipOutline = null
    // 高亮节点的id集合
    this._highlightNodeIdSet = new Set()
    // node id => node对象的映射
    this._nodeMap = new Map()
    // relationship id => relationship对象的映射
    this._relationshipMap = new Map()
    // 默认配置
    this._options = {
      arrowSize: 4,
      // 节点被点击回调函数
      onNodeClick: null,
      // 节点被双击回调函数
      onNodeDoubleClick: null,
      // 鼠标进入节点回调函数
      onNodeMouseEnter: null,
      // 鼠标离开节点回调函数
      onNodeMouseLeave: null,
      // 节点拖动开始回调函数
      onNodeDragStart: null,
      // 节点拖动结束回调函数
      onNodeDragEnd: null,
      // 双击关系回调函数
      onRelationshipDoubleClick: null,
      // 是否展示属性信息面板
      showInfoPanel: true,
      // 节点默认半径
      nodeRadius: 25,
      // 图标
      icons: {},
      // 最小碰撞体积
      minCollision: 60,
    }
    // 颜色列表
    this._colors = [
      '#68bdf6', // light blue
      '#6dce9e', // green #1
      '#faafc2', // light pink
      '#f2baf6', // purple
      '#ff928c', // light red
      '#fcea7e', // light yellow
      '#ffc766', // light orange
      '#405f9e', // navy blue
      '#a5abb6', // dark gray
      '#78cecb', // green #2,
      '#b88cbb', // dark purple
      '#ced2d9', // light gray
      '#e84646', // dark red
      '#fa5f86', // dark pink
      '#ffab1a', // dark orange
      '#fcda19', // dark yellow
      '#797b80', // black
      '#c9d96f', // pistacchio
      '#47991f', // green #3
      '#70edee', // turquoise
      '#ff75ea',  // pink
    ]
    // 已经注册过的类别到颜色的映射
    this._classes2colors = {}
    // 已经注册过的类别
    this._numClasses = 0

    if (optionsProvided) {
      // 如果有提供的选项配置, 就合并默认配置和提供的配置, 优先覆盖原先的配置
      merge(this._options, optionsProvided)
    }

    let options = this._options, that = this

    // noinspection JSUnresolvedFunction
    this._container = d3.select(selector)

    // noinspection JSUnresolvedFunction
    this._container.attr('class', 'graph-window').html('')

    if (options.showInfoPanel) {
      this._infoPanel = new InfoPanel(this._container)
    }

    // noinspection JSUnresolvedFunction
    this._svg = this._container.append('svg').
      attr('width', '100%').
      attr('height', '100%').
      attr('class', 'graph-window-svg').
      call(d3.zoom().on('zoom', function() {
        // noinspection JSUnresolvedVariable
        let scale = d3.event.transform.k,
          translate = [d3.event.transform.x, d3.event.transform.y]

        if (that._svgTranslate) {
          translate[0] += that._svgTranslate[0]
          translate[1] += that._svgTranslate[1]
        }

        if (that._svgScale) {
          scale *= that._svgScale
        }

        // noinspection JSUnresolvedFunction
        that._svg.attr('transform',
          'translate(' + translate[0] + ', ' + translate[1] + ') ' +
          'scale(' + scale + ')',
        )
      })).
      on('dblclick.zoom', null).
      append('g').
      attr('width', '100%').
      attr('height', '100%')

    // noinspection JSUnresolvedFunction
    this._svgRelationships =
      this._svg.append('g').attr('class', 'relationships')

    // noinspection JSUnresolvedFunction
    this._svgNodes = this._svg.append('g').attr('class', 'nodes')
    // 力导向图初始化
    // noinspection JSUnresolvedFunction
    this._simulation = d3.forceSimulation()
      // .velocityDecay(0.8)
      // .force('x', d3.force().strength(0.002))
      // .force('y', d3.force().strength(0.002))
      .force('collide', d3.forceCollide().radius(function(node) {
        // TODO: 最小碰撞体积与节点属性有关
        return options.minCollision + node.id
      }).iterations(2)).
      force('charge', d3.forceManyBody()).
      force('link', d3.forceLink().id(function(relationship) {
        return relationship.id
      })).
      force('center', d3.forceCenter(
        this._svg.node().parentElement.parentElement.clientWidth / 2,
        this._svg.node().parentElement.parentElement.clientHeight / 2)).
      on('tick', function() {
        that._tick()
      })
  }

  /**
   * 追加图节点和关系信息
   * @param nodes {[{id, labels, properties}]} 节点结构体数组
   * @param relationships {[{id, type, startNode, endNode, properties}]} 关系结构体数组
   * @param nodePoints {Map} 指定某些节点的初始位置 id => {x, y}
   */
  appendGraph({nodes, relationships}, nodePoints= null) {
    if (nodes && nodes.length > 0) {
      this._appendNodes(nodes, nodePoints)
    }
    if (relationships && relationships.length > 0) {
      this._appendRelationships(relationships)
    }
  }

  /**
   * 增加高亮的节点
   * @param nodeIdSet {Set} 节点id集合
   */
  addHighlightNode(nodeIdSet) {
    for (let id of nodeIdSet) {
      this._highlightNodeIdSet.add(id)
    }
  }

  /**
   * 清空高亮节点集
   */
  clearHighlightNodeSet() {
    this._highlightNodeIdSet.clear()
  }

  /**
   * 节点数量
   * @returns {number}
   */
  nodeCount() {
    return this._nodeMap.size
  }

  /**
   * 追加node, 如果有相同id, 则更新
   * @param nodes {[{id, labels, properties}]} 节点结构体信息
   * @param nodePoints {Map} 节点初始位置信息 id => {x, y}
   */
  _appendNodes(nodes, nodePoints) {
    this._registerNodes(nodes, nodePoints)
    let graphNodes = []
    for (let value of this._nodeMap.values()) {
      graphNodes.push(value)
    }
    this._setNodeDoms(graphNodes)
    this._simulation.nodes(graphNodes)
  }

  /**
   * 追加relationship, 如果有相同id, 则更新
   * @param relationships
   */
  _appendRelationships(relationships) {
    this._registerRelationships(relationships)
    let graphRelationships = []
    for (let value of this._relationshipMap.values()) {
      graphRelationships.push(value)
    }
    this._setRelationshipDoms(graphRelationships)
    this._simulation.force('link').links(graphRelationships)
  }

  /**
   * 转换并注册节点到nodeMap中
   * @param sourceNodes {[{id, labels, properties}]} 原始节点对象列表
   * @param nodePoints {Map} 节点初始位置信息 id => {x, y}
   * @private
   */
  _registerNodes(sourceNodes, nodePoints) {
    let nodeMap = this._nodeMap
    sourceNodes.forEach((node) => {
      let x, y
      if (nodePoints && nodePoints.has(node.id)) {
        x = nodePoints.get(node.id).x
        y = nodePoints.get(node.id).y
      } else {
        x = Math.random()
        y = Math.random()
      }
      let oldNode = this._nodeMap.get(node.id),
        newNode = new GraphNode(node, x, y)

      if (oldNode) {
        // 如果有旧节点的话, 要把旧节点的信息转移到新节点上, 并且将
        // 旧节点相关的关系的端点设置为新的节点, dom的更新需要节点的引用, 旧节点引用
        // 失效后会造成图数据没更新的bug
        for (let relId of oldNode.asRelTarget) {
          this._relationshipMap.get(relId).target = newNode
        }
        for (let relId of oldNode.asRelSource) {
          this._relationshipMap.get(relId).source = newNode
        }
        newNode.x = oldNode.x
        newNode.y = oldNode.y
        newNode.asRelSource = oldNode.asRelSource
        newNode.asRelTarget = oldNode.asRelTarget
      }
      nodeMap.set(node.id, newNode)
    })
  }

  /**
   * 转换并注册关系到relationshipMap中
   * @param sourceRelationships {[{id, type, properties, startNode, endNode}]}
   *  原始关系对象列表
   * @private
   */
  _registerRelationships(sourceRelationships) {
    sourceRelationships.forEach((relationship) => {
      // 设置关系的起点和终点对象
      let target = this._nodeMap.get(relationship.startNode),
        source = this._nodeMap.get(relationship.endNode)
      if (source && target) {
        this._relationshipMap.set(relationship.id, relationship)
      } else {
        // 如果找不到指定的节点, 那么表明信息缺失
        throw Error('can not find relationship node. relationship: ' +
          JSON.stringify(relationship))
      }
      this._relationshipMap.set(relationship.id,
        new GraphRelationship(relationship, source, target))
    })
  }

  /**
   * 设置关系的dom对象
   * @param relationships {[GraphRelationship]}
   * @private
   */
  _setRelationshipDoms(relationships) {
    // noinspection JSUnresolvedFunction
    let options = this._options, that = this,
      svgRelationships = this._svgRelationships.
        selectAll('.relationship').data(relationships, function(d) {
          return d.id
        }),
      relationshipEnter = svgRelationships.enter().append('g').
        attr('class', 'relationship').
        on('dblclick', function(d) {
          if (typeof options.onRelationshipDoubleClick === 'function') {
            options.onRelationshipDoubleClick(d)
          }
        }).
        on('mouseenter', function(relationship) {
          if (that._infoPanel) {
            that._setInfoPanelFromRelationship(relationship)
          }
        }).on('mouseleave', function(/*relationship*/) {
          if (that._infoPanel) {
            that._infoPanel.clear()
          }
        }),
      // 关系类型文字
      text = relationshipEnter.append('text').
        attr('class', 'text').
        attr('fill', '#000000').
        attr('font-size', '8px').
        attr('pointer-events', 'none').
        attr('text-anchor', 'middle').
        text(function(relationship) {
          return relationship.type
        }),
      // 表示关系的直线
      outline = relationshipEnter.append('path').
        attr('class', 'outline').
        attr('fill', '#a5abb6').
        attr('stroke', 'none'),
      // 鼠标移动到边上后出现的蓝边, 增大关系被选中的范围
      overlay = relationshipEnter.append('path').attr('class', 'overlay')

    svgRelationships.exit().remove()

    this._relationship = relationshipEnter.merge(svgRelationships)
    this._relationshipOutline = outline.merge(
      this._svg.selectAll('.relationship .outline'),
    )
    this._relationshipOverlay = overlay.merge(
      this._svg.selectAll('.relationship .overlay'),
    )
    this._relationshipText = text.merge(
      this._svg.selectAll('.relationship .text'),
    )
  }

  /**
   * 设置节点的dom
   * @param nodes {[GraphNode]} 节点对象列表
   * @private
   */
  _setNodeDoms(nodes) {
    // noinspection JSUnresolvedFunction
    let options = this._options, that = this,
      svgNodes = this._svgNodes.selectAll('.node').data(nodes,
        function(node) {
          return node.id
        }), // noinspection JSUnresolvedFunction
      nodeEnter = svgNodes.enter().append('g').
        attr('class', function(node) {
          let classes = 'node'
          if (that._highlightNodeIdSet.has(node.id)) {
            // 为高亮节点添加样式
            classes += ' node-highlighted'
          }
          return classes
        }).
        on('click', function(node) {
          node.fx = node.fy = null

          if (typeof options.onNodeClick === 'function') {
            options.onNodeClick(node)
          }
        }).
        on('dblclick', function(node) {
          node.fx = node.x
          node.fy = node.y
          if (typeof options.onNodeDoubleClick === 'function') {
            options.onNodeDoubleClick(node)
          }
        }).
        on('mouseenter', function(node) {
          if (that._infoPanel) {
            that._setInfoPanelFromNode(node)
          }
          if (typeof options.onNodeMouseEnter === 'function') {
            options.onNodeMouseEnter(node)
          }
        }).
        on('mouseleave', function(node) {
          if (that._infoPanel) {
            that._infoPanel.clear()
          }
          if (typeof options.onNodeMouseLeave === 'function') {
            options.onNodeMouseLeave(node)
          }
        }).
        call(d3.drag().
          on('start', function(node) {
            // noinspection JSUnresolvedVariable
            if (!d3.event.active) {
              // noinspection JSUnresolvedFunction
              that._simulation.alphaTarget(0.3).restart()
            }

            node.fx = node.x
            node.fy = node.y

            if (typeof options.onNodeDragStart === 'function') {
              options.onNodeDragStart(node)
            }
          }).
          on('drag', function(node) {
            // noinspection JSUnresolvedVariable
            node.fx = d3.event.x
            // noinspection JSUnresolvedVariable
            node.fy = d3.event.y
          }).
          on('end', function(node) {
            // noinspection JSUnresolvedVariable
            if (!d3.event.active) {
              // noinspection JSUnresolvedFunction
              that._simulation.alphaTarget(0)
            }
            if (typeof options.onNodeDragEnd === 'function') {
              options.onNodeDragEnd(node)
            }
          }),
        )
    // 节点外圈高亮圆环
    nodeEnter.append('circle').
      attr('class', 'ring').
      attr('r', options.nodeRadius * 1.16).
      append('title').
      text(function(node) {
        return node.toString()
      })
    // 节点名字
    nodeEnter.append('text').
      attr('class', 'nodeName').
      style('fill', 'block').
      attr('dx', 30).
      attr('dy', -15).
      style('font-size', '15px').
      text(function(d) {
        return d.properties['名字']
      })
    // 节点原形图片
    nodeEnter.append('circle').
      attr('class', 'outline').
      attr('r', function(node) {
        // TODO: 与节点属性相关
        return options.nodeRadius + node.id / 25
      }).
      style('fill', function(node) {
        return that._class2color(node.labels[0])
      }).
      style('stroke', function(node) {
        return darkerColor(that._class2color(node.labels[0]))
      }).
      append('title').
      text(function(node) {
        return node.toString()
      })

    // TODO: 图标
    svgNodes.exit().remove()
    this._node = nodeEnter.merge(svgNodes)
  }

  /**
   * 力导向图的tick函数, 每帧渲染
   * @private
   */
  _tick() {

    if (this._node) {
      // noinspection JSUnresolvedFunction
      this._node.attr('transform', function(node) {
        return 'translate(' + node.x + ', ' + node.y + ')'
      })
    }

    if (this._relationship) {
      // noinspection JSUnresolvedFunction
      this._relationship.attr('transform', function(relationship) {
        return 'translate(' + relationship.source.x + ', '
          + relationship.source.y +
          ') rotate(' +
          getAngle(relationship.source, relationship.target) + ')'
      })
      this._tickRelationshipsTexts()
      this._tickRelationshipsOutlines()
      this._tickRelationshipsOverlays()
    }
  }

  /**
   * 类别到颜色的映射
   * @param cls {String} 类别
   */
  _class2color(cls) {
    let color = this._classes2colors[cls]
    if (!color) {
      color = this._colors[this._numClasses % this._colors.length]
      this._classes2colors[cls] = color
      this._numClasses++
    }
    return color
  }

  /**
   * 渲染显示关系类型的字体
   * @private
   */
  _tickRelationshipsTexts() {
    // noinspection JSUnresolvedFunction
    this._relationshipText.attr('transform', function(rel) {
      let angle = (getAngle(rel.source, rel.target) + 360) % 360,
        mirror = angle > 90 && angle < 270,
        center = {x: 0, y: 0},
        n = unitaryNormalVector(rel.source, rel.target, 1),
        nWeight = mirror ? 2 : -3,
        point = {
          x: (rel.target.x - rel.source.x) * 0.5 + n.x * nWeight,
          y: (rel.target.y - rel.source.y) * 0.5 + n.y * nWeight,
        },
        rotatedPoint = rotatePoint(center, point, angle)

      return 'translate(' + rotatedPoint.x + ', ' + rotatedPoint.y +
        ') rotate(' + (mirror ? 180 : 0) + ')'
    })
  }

  /**
   * 渲染节点间的关系直线
   * @private
   */
  _tickRelationshipsOutlines() {
    let options = this._options
    // noinspection JSUnresolvedFunction
    this._relationship.each(function(/*relationship*/) {
      // 目前用不上relationship对象
      // noinspection JSUnresolvedFunction
      let relationship = d3.select(this),
        outline = relationship.select('.outline'),
        text = relationship.select('.text')

      outline.attr('d', function(rel) {
        let center = {x: 0, y: 0},
          angle = getAngle(rel.source, rel.target),
          textBoundingBox = text.node().getBBox(),
          textPadding = 5,
          u = unitaryVector(rel.source, rel.target, 1),
          textMargin = {
            x: (rel.target.x - rel.source.x -
              (textBoundingBox.width + textPadding) * u.x) * 0.5,
            y: (rel.target.y - rel.source.y -
              (textBoundingBox.width + textPadding) * u.y) * 0.5,
          },
          n = unitaryNormalVector(rel.source, rel.target, 1),
          rotatedPointA1 = rotatePoint(center, {
            x: (options.nodeRadius + 1) * u.x - n.x,
            y: (options.nodeRadius + 1) * u.y - n.y,
          }, angle),
          rotatedPointB1 = rotatePoint(center,
            {x: textMargin.x - n.x, y: textMargin.y - n.y}, angle),
          rotatedPointC1 = rotatePoint(center,
            {x: textMargin.x, y: textMargin.y}, angle),
          rotatedPointD1 = rotatePoint(center, {
            x: (options.nodeRadius + 1) * u.x,
            y: (options.nodeRadius + 1) * u.y,
          }, angle),
          rotatedPointA2 = rotatePoint(center, {
            x: rel.target.x - rel.source.x - textMargin.x - n.x,
            y: rel.target.y - rel.source.y - textMargin.y - n.y,
          }, angle),
          rotatedPointB2 = rotatePoint(center, {
            x: rel.target.x - rel.source.x - (options.nodeRadius + 1) *
              u.x - n.x - u.x * options.arrowSize,
            y: rel.target.y - rel.source.y - (options.nodeRadius + 1) *
              u.y - n.y - u.y * options.arrowSize,
          }, angle),
          rotatedPointC2 = rotatePoint(center, {
            x: rel.target.x - rel.source.x - (options.nodeRadius + 1) *
              u.x - n.x + (n.x - u.x) * options.arrowSize,
            y: rel.target.y - rel.source.y - (options.nodeRadius + 1) *
              u.y - n.y + (n.y - u.y) * options.arrowSize,
          }, angle),
          rotatedPointD2 = rotatePoint(center, {
            x: rel.target.x - rel.source.x - (options.nodeRadius + 1) * u.x,
            y: rel.target.y - rel.source.y - (options.nodeRadius + 1) * u.y,
          }, angle),
          rotatedPointE2 = rotatePoint(center, {
            x: rel.target.x - rel.source.x - (options.nodeRadius + 1) *
              u.x + (-n.x - u.x) * options.arrowSize,
            y: rel.target.y - rel.source.y - (options.nodeRadius + 1) *
              u.y + (-n.y - u.y) * options.arrowSize,
          }, angle),
          rotatedPointF2 = rotatePoint(center, {
            x: rel.target.x - rel.source.x - (options.nodeRadius + 1) *
              u.x - u.x * options.arrowSize,
            y: rel.target.y - rel.source.y - (options.nodeRadius + 1) *
              u.y - u.y * options.arrowSize,
          }, angle),
          rotatedPointG2 = rotatePoint(center, {
            x: rel.target.x - rel.source.x - textMargin.x,
            y: rel.target.y - rel.source.y - textMargin.y,
          }, angle)

        return 'M ' + rotatedPointA1.x + ' ' + rotatedPointA1.y +
          ' L ' + rotatedPointB1.x + ' ' + rotatedPointB1.y +
          ' L ' + rotatedPointC1.x + ' ' + rotatedPointC1.y +
          ' L ' + rotatedPointD1.x + ' ' + rotatedPointD1.y +
          ' Z M ' + rotatedPointA2.x + ' ' + rotatedPointA2.y +
          ' L ' + rotatedPointB2.x + ' ' + rotatedPointB2.y +
          ' L ' + rotatedPointC2.x + ' ' + rotatedPointC2.y +
          ' L ' + rotatedPointD2.x + ' ' + rotatedPointD2.y +
          ' L ' + rotatedPointE2.x + ' ' + rotatedPointE2.y +
          ' L ' + rotatedPointF2.x + ' ' + rotatedPointF2.y +
          ' L ' + rotatedPointG2.x + ' ' + rotatedPointG2.y +
          ' Z'
      })
    })
  }

  _tickRelationshipsOverlays() {
    // noinspection JSUnresolvedFunction
    this._relationshipOverlay.attr('d', function(d) {
      let center = {x: 0, y: 0},
        angle = getAngle(d.source, d.target),
        n1 = unitaryNormalVector(d.source, d.target, 1),
        n = unitaryNormalVector(d.source, d.target, 50),
        rotatedPointA = rotatePoint(center, {x: 0 - n.x, y: 0 - n.y},
          angle),
        rotatedPointB = rotatePoint(center, {
          x: d.target.x - d.source.x - n.x,
          y: d.target.y - d.source.y - n.y,
        }, angle),
        rotatedPointC = rotatePoint(center, {
          x: d.target.x - d.source.x + n.x - n1.x,
          y: d.target.y - d.source.y + n.y - n1.y,
        }, angle),
        rotatedPointD = rotatePoint(center,
          {x: n.x - n1.x, y: n.y - n1.y}, angle)

      return 'M ' + rotatedPointA.x + ' ' + rotatedPointA.y +
        ' L ' + rotatedPointB.x + ' ' + rotatedPointB.y +
        ' L ' + rotatedPointC.x + ' ' + rotatedPointC.y +
        ' L ' + rotatedPointD.x + ' ' + rotatedPointD.y +
        ' Z'
    })
  }

  /**
   * 追加信息面板的标签项
   * @param labels {[string]} 标签
   * @private
   */
  _appendInfoPanelLabels(labels) {
    if (labels) {
      labels.forEach((label) => {
        // 这里的class对应了css里的一个样式, 用于表示标签或者关系类型
        this._infoPanel.appendElement('class', label, undefined, {
          backgroundColor: () => {
            return this._class2color(label)
          },
          borderColor: () => {
            return darkerColor(this._class2color(label))
          },
          color: () => {
            return '#ffffff'
          },
        })
      })
    }
  }

  /**
   * 追加信息面板属性项
   * @param property {string} 属性名
   * @param value {string} 属性值
   * @private
   */
  _appendInfoPanelProperty(property, value) {
    // 信息面板的id
    // property对应了css的一个样式
    this._infoPanel.appendElement('property', property, value, {
      backgroundColor: () => {
        return '#ffffff'
      },
      borderColor: () => {
        return darkerColor(GraphWindow._defaultColor())
      },
      color: () => {
        return '#181818'
      },
    })
  }

  /**
   * 根据节点信息设置信息面板
   * @param node 节点对象
   * @private
   */
  _setInfoPanelFromNode(node) {
    this._infoPanel.clear()
    this._appendInfoPanelProperty('&lt;id&gt;', node.id)
    this._appendInfoPanelLabels(node.labels)
    this._infoPanel.appendBr()

    let that = this
    // 每个属性项都对应一个信息面板项
    Object.keys(node.properties).forEach((property) => {
      that._appendInfoPanelProperty(property,
        JSON.stringify(node.properties[property]))
    })
  }

  /**
   * 根据关系信息设置信息面板
   * @param relationship 关系对象
   * @private
   */
  _setInfoPanelFromRelationship(relationship) {
    this._infoPanel.clear()
    this._appendInfoPanelProperty('&lt;id&gt;', relationship.id)
    this._appendInfoPanelLabels([relationship.type])

    let that = this
    // 每个属性项都对应一个信息面板项
    Object.keys(relationship.properties).forEach((property) => {
      that._appendInfoPanelProperty(property,
        JSON.stringify(relationship.properties[property]))
    })
  }

  /**
   * 默认颜色
   * @returns string
   */
  static _defaultColor() {
    return '#a5abb6'
  }
}

export default GraphWindow