<template>
  <el-card class="card">
    <template #header class="clear-fix">
      <el-row>
        <el-col :span="8">
          <h3 v-if="cardTitle && cardTitle !== ''">{{ cardTitle }}</h3>
        </el-col>
        <el-col :span="16" style="float:right">
          <el-button style="float:right" type="primary" size="medium" round
                     @click="clickCloseButton">关闭
          </el-button>
        </el-col>
      </el-row>
    </template>
    <pagination-entity-table
        title="根据属性搜索到的节点" v-if="nodeInPropertyCount > 0"
        request-method="POST"
        request-data-url="/api/search/node/property"
        request-count-url="/api/search/node/property/count"
        :request-param="requestParam"
        :initial-total-count="nodeInPropertyCount"
        entity-type="node"
        :table-identity="'search-result-card-' + cardId + '-in-node-property'"
    />
    <pagination-entity-table
        title="根据标签搜索到的节点" v-if="nodeInLabelCount > 0"
        request-method="POST"
        request-data-url="/api/search/node/label"
        request-count-url="/api/search/node/label/count"
        :request-param="requestParam"
        :initial-total-count="nodeInLabelCount"
        entity-type="node"
        :table-identity="'search-result-card-' + cardId + '-in-node-label'"
    />
    <pagination-entity-table
        title="根据属性搜索到的关系" v-if="relationshipInPropertyCount > 0"
        request-method="POST"
        request-data-url="/api/search/relationship/property"
        request-count-url="/api/search/relationship/property/count"
        :request-param="requestParam"
        :initial-total-count="relationshipInPropertyCount"
        entity-type="relationship"
        :table-identity="'search-result-card-' + cardId + '-in-relationship-property'"
    />
    <pagination-entity-table
        title="根据类型搜索到的关系" v-if="relationshipInTypeCount > 0"
        request-method="POST"
        request-data-url="/api/search/relationship/type"
        request-count-url="/api/search/relationship/type/count"
        :request-param="requestParam"
        :initial-total-count="relationshipInTypeCount"
        entity-type="relationship"
        :table-identity="'search-result-card-' + cardId + '-in-relationship-type'"
    />
  </el-card>
</template>

<script>
import './table-common.css'
import {defineAsyncComponent} from 'vue'

const PaginationEntityTable = defineAsyncComponent(() => import('./PaginationEntityTable.vue'))

export default {
  name: 'SearchResultTablesCard',
  components: {PaginationEntityTable},
  data() {
    return {
      nodeInPropertyCount: 0,
      nodeInLabelCount: 0,
      relationshipInPropertyCount: 0,
      relationshipInTypeCount: 0,
      requestParam: {},
      key: '',
      keyId: -1
    }
  },
  props: {
    cardId: Number,
    cardData: Object,
    cardTitle: String,
  },
  mounted() {
    this.nodeInPropertyCount = this.cardData.nodeInPropertyCount
    this.nodeInLabelCount = this.cardData.nodeInLabelCount
    this.relationshipInPropertyCount = this.cardData.relationshipInPropertyCount
    this.relationshipInTypeCount = this.cardData.relationshipInTypeCount
    this.key = this.cardData.key
    this.requestParam = this.cardData.requestParam
    let keyId = Number(this.key)
    if (Number.isInteger(keyId) && keyId >= 0) {
      this.keyId = keyId
    }
  },
  methods: {
    clickCloseButton() {
      this.$emit('card-close', this.cardId)
    },
  },
}
</script>

<style scoped>
.card {
  width: 100%;
  margin-right: auto;
  margin-left: auto;
  margin-top: 10px;
  bottom: 10px;
  padding: 10px;
}

.clear-fix:before,
.clear-fix:after {
  display: table;
  content: "";
}

/*noinspection CssUnusedSymbol*/
.el-tag + .el-tag {
  margin-left: 10px;
  margin-bottom: 10px;
}

</style>