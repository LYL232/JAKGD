<template>
  <el-dialog title="修改密码" v-model="visible" width="35%" center>
    <el-form ref="form" :model="form" label-width="80px" style="width: 95%">
      <el-form-item label="新密码">
        <el-input type="password" v-model="form.password"></el-input>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input type="password" v-model="form.repeat" autosize></el-input>
      </el-form-item>
    </el-form>
    <template #footer class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="clickConfirm" :loading="loading">确 定</el-button>
    </template>
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
        repeat: '',
      },
    }
  },
  methods: {
    show() {
      this.visible = true
    },
    clickConfirm() {
      if (this.form.password !== this.form.repeat) {
        this.$notify.info({
          title: '输入错误',
          message: '新密码和重复密码不匹配',
        })
        return
      }
      this.loading = true
      this.axios.post('/api/user/password', {
        password: this.form.password.trim(),
      }).then(() => {
        this.$notify({
          title: '成功',
          message: '修改成功',
          type: 'success',
        })
        this.visible = false
        this.loading = false

        // 重新登录
        this.axios.post('/login', {
          username: this.globalData.user.username,
          password: this.form.password,
        }).then((response) => {
          this.bus.$emit('authorizationChange', {
            username: this.globalData.user.username,
            authorization: response.headers['authorization'],
          })
        }).catch((err) => {
          this.loading = false
          this.$notify.error({
            title: '请求错误',
            message: '重新认证失败',
          })
          this.bus.$emit('authorizationChange', {
            username: null,
            authorization: null,
          })
          console.log(err.response)
        })
      }).catch((err) => {
        this.util.errorHint(err, '修改失败')
        this.loading = false
      })
    },
  },
}
</script>

<style scoped>

</style>