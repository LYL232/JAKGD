<template>
  <el-card class="card">
    <template #header class="clear-fix">
      <el-row>
        <el-col :span="8">
          <h3>{{ cardTitle }}</h3>
        </el-col>
        <el-col :span="16" style="float:right">
          <el-button style="float:right" type="primary" size="medium" round
                     @click="clickCloseButton">关闭
          </el-button>
        </el-col>
      </el-row>
    </template>
    <pagination-entity-table
        title="节点" v-if="nodeCount > 0"
        request-method="POST"
        request-data-url="/api/search/node/property"
        request-count-url="/api/search/node/property/count"
        :request-param="requestBody"
        :initial-total-count="nodeCount"
        entity-type="node"
        :table-identity="'my-creation-card-' + cardId + '-node'"
    />
    <pagination-entity-table
        title="关系" v-if="relationshipCount > 0"
        request-method="POST"
        request-data-url="/api/search/relationship/property"
        request-count-url="/api/search/relationship/property/count"
        :request-param="requestBody"
        :initial-total-count="relationshipCount"
        entity-type="relationship"
        :table-identity="'my-creation-card-' + cardId + '-relationship'"
    />
  </el-card>
</template>

<script>
import './table-common.css'
import {defineAsyncComponent} from 'vue'

const PaginationEntityTable = defineAsyncComponent(() => import('./PaginationEntityTable.vue'))

export default {
  name: 'MineCreationCard',
  components: {
    PaginationEntityTable
  },
  data() {
    return {
      nodeCount: 0,
      relationshipCount: 0,
      requestBody: {},
    }
  },
  props: {
    cardId: Number,
    cardData: Object,
    cardTitle: String,
  },
  mounted() {
    this.nodeCount = this.cardData.nodeCount
    this.relationshipCount = this.cardData.relationshipCount
    this.requestBody = this.cardData.requestBody
  },
  methods: {
    clickCloseButton() {
      this.$emit('card-close', this.cardId)
    },
  },
}
</script>

<style scoped>

</style>