<template>
  <el-container style="align-items: center">
    <el-col :span="22">
      <h4>{{ title }}</h4>
    </el-col>
    <el-col :span="2" style="float:right">
      <el-button type="primary" @click="refreshTotalCount" round v-loading="loading">刷新</el-button>
    </el-col>
  </el-container>
  <div v-if="totalCount > 0" style="text-align: center">
    <entity-table
        :entity-type="entityType"
        :entity-data="(entityType === 'node') ? nodeData : relationshipData"
        :node-map="nodeMap"
        :table-identity="tableIdentity"
        :loading="loading"
    />
    <el-pagination
        style="margin-top: 10px"
        @size-change="pageSizeChange"
        @current-change="pageChange"
        :current-page="page"
        :page-sizes="[8, 16, 32, 64, 128]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalCount"
        v-loading="loading"
    />
    <el-divider/>
  </div>
  <span v-if="totalCount === 0">没有数据</span>
</template>

<script>
import {defineAsyncComponent} from 'vue'

const PopoverNodeDirector = defineAsyncComponent(() => import('../../node/PopoverNodeDirector.vue')),
    PropertyPopoverButton = defineAsyncComponent(() => import('../../node/PropertyPopoverButton.vue')),
    EntityTable = defineAsyncComponent(() => import('./EntityTable.vue'))

export default {
  name: 'PaginationRelationshipTable',
  components: {
    PopoverNodeDirector,
    PropertyPopoverButton,
    EntityTable
  },
  props: {
    tableIdentity: String,
    requestMethod: String,
    requestDataUrl: String,
    requestCountUrl: String,
    requestParam: Object,
    title: String,
    initialTotalCount: Number,
    entityType: String
  },
  data() {
    return {
      nodeMap: new Map(),
      nodeData: [],
      relationshipData: [],
      totalCount: 0,
      page: 1,
      pageSize: 8,
      loading: true,
    }
  },
  computed: {
    totalPages() {
      let totPages = Math.floor(this.totalCount / this.pageSize)
      if (totPages * this.pageSize < this.totalCount) {
        return totPages + 1
      }
      return totPages
    }
  },
  mounted() {
    this.totalCount = this.initialTotalCount
    if (this.totalCount > 0) {
      this.pageChange(1)
    }
  },
  methods: {
    mapTagName(tag) {
      return this.globalData.nodeLabelNameMap(tag)
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
      this.pageChange(1)
    },
    pageChange(page) {
      if (page <= 0 || page > this.totalPages) {
        // todo: warning
        return
      }
      this.loading = true
      let skip = (page - 1) * this.pageSize
      this.getRequest(
          this.requestDataUrl, skip, this.pageSize
      ).then(res => {
        this.refreshTableData(res.data)
        this.loading = false
        this.page = page
      }).catch(err => {
        this.loading = false
        this.util.errorHint(err, '获取表格第' + page + '页失败')
      })
    },
    refreshTotalCount() {
      this.loading = true
      this.getRequest(
          this.requestCountUrl
      ).then(res => {
        this.totalCount = res.data
        if (this.totalCount > 0) {
          this.pageChange(1)
        } else {
          this.loading = false
        }
      }).catch(err => {
        this.loading = false
        this.util.errorHint(err, '获取表格记录数量失败')
      })
    },
    getRequest(url, skip, limit) {
      switch (this.requestMethod) {
      case 'POST': {
        let requestBody = {...this.requestParam}
        skip = Number(skip)
        if (Number.isInteger(skip)) {
          requestBody.skip = skip
        }
        limit = Number(limit)
        if (Number.isInteger(limit)) {
          requestBody.limit = limit
        }
        return this.axios.post(url, requestBody)
      }
      default: {
        //todo: warning
        break
      }
      }
    },
    refreshTableData({nodes, relationships}) {
      this.nodeMap.clear()
      if (nodes) {
        nodes.forEach(node => {
          if (!this.nodeMap.has(node.id)) {
            this.nodeMap.set(node.id, node)
          }
        })
      }
      this.nodeData = nodes
      this.relationshipData = relationships
    }
  }
}
</script>

<style scoped>

</style>