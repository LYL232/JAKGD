<template>
  <el-button type="primary" round @click="refresh">刷新</el-button>
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
    <el-pagination
        style="margin-top: 10px"
        @size-change="pageSizeChange"
        @current-change="pageChange"
        :current-page="page"
        :page-sizes="[8, 16, 32, 64]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="fileRecords.length"
        v-loading="loading"
    />
  </div>
</template>

<script>
export default {
  name: 'MyFilePage',
  data() {
    return {
      fileRecords: [],
      displayRecords: [],
      loading: false,
      pageSize: 8,
      page: 1,
    }
  },
  computed: {
    tableData() {
      let tableData = []
      this.displayRecords.forEach(path => {
        tableData.push({
          path,
          cutPath: path.replace('/' + this.globalData.user.username, ''),
          url: window.location.origin + '/media/' + path
        })
      })
      return tableData
    },
    totalPages() {
      let totPages = Math.floor(this.fileRecords.length / this.pageSize)
      if (totPages * this.pageSize < this.fileRecords.length) {
        return totPages + 1
      }
      return totPages
    }
  },
  mounted() {
    this.refresh()
  },
  methods: {
    refresh() {
      this.loading = true
      this.axios.get(
          '/api/file'
      ).then(res => {
        this.loading = false
        this.fileRecords = res.data
        if (this.fileRecords.length > 0) {
          this.pageChange(1)
        }
      }).catch(err => {
        this.loading = false
        this.util.errorHint(err, '获取图片文件信息失败')
      })
    },
    pageSizeChange(newSize) {
      newSize = Number(newSize)
      if (!Number.isInteger(newSize) || newSize <= 0) {
        //todo: warning
        return
      }
      if (newSize === this.pageSize) {
        return
      }
      this.pageSize = newSize
    },
    pageChange(page) {
      if (page <= 0 || page > this.totalPages) {
        // todo: warning
        return
      }
      let base = (page - 1) * this.pageSize, displayRecords = []
      for (let i = 0; i < this.pageSize && i + base < this.fileRecords.length; ++i) {
        displayRecords.push(this.fileRecords[i + base])
      }
      this.displayRecords = displayRecords
      this.page = page
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
          this.refresh()
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