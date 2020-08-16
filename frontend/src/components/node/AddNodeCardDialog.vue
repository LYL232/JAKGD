<template>
  <el-dialog title="选择增加的卡片" :visible.sync="visible" width="30%" center>
    <el-input class="input" placeholder="卡片名称" v-model="cardName" clearable></el-input>
    <el-select class="input" v-model="value" placeholder="请选择增加的卡片类型">
      <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
      </el-option>
    </el-select>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="clickConfirm">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'AddNodeCardDialog',
  data() {
    return {
      visible: false,
      options: [
        {
          value: 'markdown',
          label: 'markdown',
        },
      ],
      value: '',
      cardName: '',
    }
  },
  props: {
    node: {
      type: Object,
      default: null,
    },
  },
  methods: {
    show() {
      this.visible = true
    },
    clickConfirm() {
      this.value = this.value.trim()

      if (!this.value || this.value === '') {
        this.$message.warning('请选择卡片类型')
        return
      }

      this.cardName = this.cardName.trim()
      if (!this.cardName || this.cardName === '') {
        this.$message.warning('卡片名称不能为空')
        return
      }

      if (this.value === 'markdown') {

        let docName = this.cardName

        if (!(/^[a-zA-Z_\u4E00-\u9FA5]([A-Za-z\u4E00-\u9FA50-9]){1,20}$/.exec(docName))) {
          this.$message.warning('非法的文档名: 请输入中英文字母, 数字组合的名字, 长度大于等于2小于等于20')
          return
        }
        this.visible = false
        this.value = ''
        this.cardName = ''

        this.bus.$emit('newTab', {
          doc: {
            content: '',
            name: docName,
          },
          node: this.node,
          type: 'md-editor',
        })
      } else {
        this.$message.warning('不支持卡片类型:' + this.value)
      }
    },
  },
}
</script>

<style scoped>
  .input {
    margin: 10px;
    width: 90%
  }
</style>