<template>
  <el-dialog :title="node?'修改信息' : '创建节点'" :visible.sync="visible" width="750px" center>
    <el-form ref="form" :model="form" label-width="150px" style="width: 95%">
      <el-form-item v-if="!node" label="基础类型">
        <el-select
                v-model="form.nodeBaseType"
                @change="nodeBaseTypeSelected"
                placeholder="请选择基础类型">
          <el-option
                  v-for="item in nodeBaseTypeOption"
                  :key="'node-base-type-option-' + item.value"
                  :label="item.label"
                  :value="item.value"/>
        </el-select>
      </el-form-item>
    </el-form>
    <property-editor ref="propertyEditor"/>
    <i class="el-icon-collection-tag" style="margin-right: 10px"/>
    <el-tag :key="'label-' + tag" v-for="(tag, i) in tags" :closable="!tag.startsWith('__')"
            :disable-transitions="false"
            :type="colors[i % colors.length]"
            @close="handleClose(tag)">
      {{mapTagName(tag)}}
    </el-tag>
    <el-input class="input-new-tag" v-if="newTagInputVisible" v-model="newTagInputValue"
              ref="saveTagInput" size="small" @keyup.enter.native="handleLabelInput"
              @blur="handleLabelInput"></el-input>
    <el-button v-else class="button-new-tag" size="small" @click="showInput">+ 新标签</el-button>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" :loading="loading" @click="clickConfirm">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
const PropertyEditor = () => import ('./PropertyEditor')

export default {
  name: 'EditNodeDialog',
  components: {PropertyEditor},
  data() {
    return {
      tags: [],

      newTagInputValue: '',
      newTagInputVisible: false,

      colors: ['success', 'primary', 'info', 'danger', 'warning'],
      visible: false,
      form: {
        nodeBaseType: '',
      },
      defaultPropertyInputConfig: {
        label: '',
        type: 'text',
      },
      loading: false,
      nodeBaseTypeOption: [
        {
          label: '无',
          value: '',
        },
        {
          label: '知识点',
          value: '__knowledge',
        },
      ],
    }
  },
  props: {
    node: {
      type: Object,
      default: null,
    },
  },
  methods: {
    mapTagName(tag) {
      return this.globalData.nodeLabelNameMap(tag)
    },

    handleLabelInput() {
      // 新标签输入框确认
      let inputValue = this.newTagInputValue
      if (inputValue) {
        inputValue = inputValue.trim()
        if (!(/^[a-zA-Z_\u4E00-\u9FA5]([A-Za-z\u4E00-\u9FA50-9]){1,20}$/.exec(inputValue))) {
          this.$notify.info({
            title: '输入错误',
            message: '非法的标签格式: 请输入中英文字母, 数字组合的属性名, 长度大于等于2小于等于20',
          })
          return
        }
        let unique = true
        // 判断是否是唯一的
        for (let i = 0; i < this.tags.length; ++i) {
          if (this.tags[i] === inputValue) {
            unique = false
            break
          }
        }
        if (unique) {
          this.tags.push(inputValue)
        }
      }
      this.newTagInputVisible = false
      this.newTagInputValue = ''
    },

    handleClose(tag) {
      // 点击删除标签按钮
      this.tags.splice(this.tags.indexOf(tag), 1)
    },

    showInput() {
      this.newTagInputVisible = true
      this.$nextTick(function() {
        this.$refs.saveTagInput.$refs.input.focus()
      })
    },

    /**
     * 添加新的属性输入框
     * @param name 属性名
     * @param value 初始属性值
     */
    addNewPropertyInput(name, value) {
      let deletable = true,
        nodeBaseTypeRequireProperties = this.globalData.nodeBaseTypeRequireProperties
      if (this.node) {
        for (let label of this.node.labels) {
          if (label.startsWith('__')
            && nodeBaseTypeRequireProperties[label]
            && nodeBaseTypeRequireProperties[label].has(name)) {
            deletable = false
            break
          }
        }
      } else {
        if (nodeBaseTypeRequireProperties[this.form.nodeBaseType]) {
          deletable = !(nodeBaseTypeRequireProperties[this.form.nodeBaseType].has(name))
        }
      }
      this.$refs.propertyEditor.addNewPropertyInput(name, value, deletable, false)
    },

    show() {
      if (this.node) {
        this.tags = []
        // 复制
        this.node.labels.forEach(label => {
          this.tags.push(label)
        })

        this.$nextTick(() => {
          for (let label of this.node.labels) {
            if (label === '__knowledge') {
              this.showKnowledgeRequireInput()
              break
            }
          }
          this.$refs.propertyEditor.clearInput()
          for (let name in this.node.properties) {
            if (Object.prototype.hasOwnProperty.call(this.node.properties, name)) {
              this.addNewPropertyInput(name, this.node.properties[name])
            }
          }
        })
      }
      this.visible = true
    },

    clickConfirm() {
      let requestData = this.getRequestData()
      if (!requestData) {
        return
      }

      this.loading = true

      let requestUrl = '/api/node/'

      if (this.node) {
        for (let label of this.node.labels) {
          switch (label) {
          case '__knowledge': {
            requestUrl = '/api/knowledge/'
            break
          }
          }
          if (requestUrl !== '/api/node/') {
            break
          }
        }
        this.sendUpdateRequest(requestUrl + this.node.id, requestData)
      } else {
        switch (this.form.nodeBaseType) {
        case '__knowledge': {
          requestUrl = '/api/knowledge/'
          break
        }
        }
        this.sendCreateRequest(requestUrl, requestData)
      }
    },

    showKnowledgeRequireInput() {
      if (this.node) {
        this.addNewPropertyInput('name', this.node.properties.name)
        this.addNewPropertyInput('summary', this.node.properties.summary)
      } else {
        this.addNewPropertyInput('name', '')
        this.addNewPropertyInput('summary', '')
      }
    },

    getNotInternalLabels() {
      let notInternalLabels = []
      this.tags.forEach(tag => {
        if (!tag.startsWith('__')) {
          notInternalLabels.push(tag)
        }
      })
      return notInternalLabels
    },

    /**
     * 获取请求数据
     * @returns {{}|null}
     */
    getRequestData() {
      let res = {}, propertyEditor = this.$refs.propertyEditor
      for (let property of propertyEditor.getFormProperties()) {
        if (!property.value || (property.value = property.value.trim()) === '') {
          this.$notify.info({
            title: '输入错误',
            message: '属性值不能为空: ' + property.name,
          })
          return null
        }
        res[property.name] = property.value
      }
      res.labels = this.getNotInternalLabels()
      return res
    },

    sendCreateRequest(url, postBody) {
      this.axios.post(url, postBody).then(response => {
        this.requestSuccess()
        this.$emit('created', response.data)
      }).catch(err => {
        this.requestFailed(err)
      })
    },

    sendUpdateRequest(url, postBody) {
      this.axios.put(url, postBody).then(() => {
        this.requestSuccess()
        this.$emit('edited')
      }).catch(err => {
        this.requestFailed(err)
      })
    },

    requestSuccess() {
      this.$notify({
        title: '成功',
        type: 'success',
        message: '操作完成',
      })
      this.visible = false
      this.loading = false
      this.newPropertyName = ''
      this.$refs.propertyEditor.clearInput()
      this.form.nodeBaseType = ''
      this.tags = []
    },

    requestFailed(err) {
      this.util.errorHint(err, '操作失败')
      this.loading = false
      this.show()
    },

    nodeBaseTypeSelected(value) {
      if (value.startsWith('__')) {
        this.tags = [value]
        let require = this.globalData.nodeBaseTypeRequireProperties
        if (require[value]) {
          for (let name of require[value]) {
            this.addNewPropertyInput(name, '', false)
          }
        }
      } else {
        this.tags = []
        this.$refs.propertyEditor.getFormProperties().forEach(property => {
          property.deletable = true
        })
      }
    },
  },
}
</script>

<style scoped>
  /*noinspection CssUnusedSymbol*/
  .el-tag + .el-tag {
    margin-left: 10px;
    margin-bottom: 10px;
  }

  .button-new-tag {
    margin-left: 10px;
    height: 32px;
    line-height: 30px;
    padding-top: 0;
    padding-bottom: 0;
  }

  .input-new-tag {
    width: 90px;
    margin-left: 10px;
    vertical-align: bottom;
  }
</style>