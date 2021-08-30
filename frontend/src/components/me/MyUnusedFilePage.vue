<template>
  <el-button-group>
    <el-button type="primary" round @click="refresh">刷新</el-button>
  </el-button-group>
  <el-divider/>
  <div style="text-align: center">
    <el-table :data="tableData" v-loading="loading">
      <el-table-column width="auto" property="cutPath" label="文件路径"/>
      <el-table-column width="auto" label="图像">
        <template #default="scope">
          <el-image
              style="width: 200px; height: 200px"
              :src="scope.row.url"
              :preview-src-list="[scope.row.url]"
              fit="scale-down"
          />
        </template>
      </el-table-column>
      <el-table-column width="auto" label="操作">
        <template #default="scope">
          <el-button type="danger" icon="el-icon-delete" round
                     @click="clickDeleteButton(scope.row.path)">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'MyUnusedFilePage',
  data() {
    return {
      fileRecords: [],
      loading: false,
    }
  },
  computed: {
    tableData() {
      let tableData = []
      this.fileRecords.forEach(path => {
        tableData.push({
          path,
          cutPath: path.replace('/' + this.globalData.user.username, ''),
          url: window.location.origin + '/media/' + path
        })
      })
      return tableData
    },
  },
  mounted() {
    this.refresh()
  },
  methods: {
    refresh() {
      this.loading = true
      this.fileRecords = []
      this.axios.get(
          '/api/file'
      ).then(res => {
        let allFiles = res.data
        if (allFiles.length === 0) {
          return
        }
        this.loading = false
        allFiles.forEach(path => {
          this.axios.get(
              '/api/document/mine/contain', {params: {key: path}}
          ).then(res => {
            if (res.data > 0) {
              return
            }
            this.fileRecords.push(path)
          }).catch(err => {
            this.util.errorHint(err, '判断文档中是否使用文件: ' + path + ' 失败')
          })
        })
      }).catch(err => {
        this.loading = false
        this.util.errorHint(err, '获取图片文件信息失败')
      })
    },
    clickDeleteButton(path) {
      this.$confirm('永久删除该文件?', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.loading = true
        this.axios.delete(
            '/api/file', {params: {path}}
        ).then(() => {
          for (let i = 0; i < this.fileRecords.length; ++i) {
            if (this.fileRecords[i] === path) {
              this.fileRecords.splice(i, 1)
              break
            }
          }
          this.$notify({
            title: '成功',
            type: 'success',
            message: '删除成功',
          })
          this.loading = false
        }).catch(err => {
          this.loading = false
          this.util.errorHint(err, '删除文件: ' + path + ' 失败')
        })
      }).catch(() => {})
    },
  }
}
</script>

<style scoped>

</style>