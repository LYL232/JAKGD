<template>
  <h3>修改密码</h3>
  <el-divider/>
  <div style="height: 600px; width: 600px; margin-top: 10px">
    <el-form ref="form" :model="form" label-width="80px" label-position="left">
      <el-form-item label="新密码">
        <el-input type="password" v-model="form.password" show-password/>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input type="password" v-model="form.repeat" show-password/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="clickConfirm" :loading="loading">
          确 定
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>

export default {
  name: 'ChangePasswordPage',
  data() {
    return {
      loading: false,
      form: {
        password: '',
        repeat: '',
      },
    }
  },
  methods: {
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