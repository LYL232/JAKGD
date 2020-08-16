<template>
  <el-dialog title="修改密码" :visible.sync="visible" width="35%" center>
    <el-form ref="form" :model="form" label-width="80px" style="width: 95%">
      <el-form-item label="新密码">
        <el-input type="password" v-model="form.password"></el-input>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input type="password" v-model="form.repeat" autosize></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="clickConfirm" :loading="loading">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>

export default {
  name: 'ChangePasswordDialog',
  data() {
    return {
      visible: false,
      loading: false,
      form: {
        password: '',
        repeat: ''
      }
    }
  },
  methods: {
    show() {
      this.visible = true
    },
    clickConfirm() {
      if (this.form.password !== this.form.repeat) {
        this.$message('新密码和重复密码不匹配')
        return
      }
      this.loading = true
      this.axios.post('/api/user/password', {
        password: this.form.password.trim()
      }).then(() => {
        this.$message({
          message: '修改成功',
          type: 'success'
        })
        this.visible = false
        this.loading = false

        // 重新登录
        this.axios.post('/login', {
          username: this.globalData.username,
          password: this.form.password,
        }).then((response) => {
          this.bus.$emit('authorizationChange', {
            username: this.globalData.username,
            authorization: response.headers['authorization']
          })
        }).catch((err) => {
          this.loading = false
          this.$message.error('重新认证失败')
          this.bus.$emit('authorizationChange', {
            username: null,
            authorization: null
          })
          console.log(err.response)
        })
      }).catch((err) => {
        this.$message.error('修改失败')
        this.loading = false
        console.log(err.response)
      })
    },
  }
}
</script>

<style scoped>

</style>