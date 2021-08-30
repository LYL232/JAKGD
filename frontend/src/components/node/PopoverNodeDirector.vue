<template>
  <el-popover placement="auto" trigger="click" v-model:visible="show" width="400px">
    <div style="width: 400px">
      <el-col style="align-items: center">
        <el-table v-if="node" :data="nodeData">
          <el-table-column width="150" property="name" label="节点"/>
          <el-table-column width="250" property="value">
            <template #header>
              <el-button @click="clickNodeButton" round type="primary" :loading="loading"
                         style="float:right;" icon="el-icon-view"/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col style="align-items: center">
        <el-table v-if="relationship" :data="relationshipData">
          <el-table-column width="150" property="name" label="关系"/>
          <el-table-column width="250" property="value">
            <template #header>
              <el-button-group style="float:right;">
                <el-button type="primary" round icon="el-icon-edit" :loading="loading"
                           @click="clickEditRelationshipButton"/>
                <el-button @click="clickDeleteRelationshipButton" type="danger"
                           round icon="el-icon-delete" :loading="loading"/>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </div>
    <template #reference>
      <el-button round>{{ getDisplayInfo() }}</el-button>
    </template>
  </el-popover>
</template>

<script>
export default {
  name: 'PopoverButton',
  emits: ['relationship-deleted', 'edit-relationship', 'newTab'],
  data() {
    return {
      loading: false,
      show: false,
    }
  },
  computed: {
    relationshipData() {
      let res = []
      if (this.relationship) {
        res.push({
          name: 'id',
          value: this.relationship.id,
        }, {
          name: '关系类型',
          value: this.globalData.relationshipTypeNameMap(this.relationship.type),
        })
        for (let name in this.relationship.properties) {
          if (Object.prototype.hasOwnProperty.call(this.relationship.properties, name)) {
            res.push({
              name: this.globalData.propertyNameMap(name),
              value: this.relationship.properties[name],
            })
          }
        }
      }
      return res
    },
    nodeData() {
      let res = []
      if (this.node) {
        res.push({
          name: 'id',
          value: this.node.id,
        })
        for (let name in this.node.properties) {
          if (Object.prototype.hasOwnProperty.call(this.node.properties, name)) {
            res.push({
              name: this.globalData.propertyNameMap(name),
              value: this.node.properties[name],
            })
          }
        }
      }
      return res
    },
  },
  props: {
    // {
    //  id, summary
    // }
    node: {
      type: Object,
      default() {
        return null
      },
    },
    // {
    //  id, startNode, endNode, type, properties
    // }
    relationship: {
      type: Object,
      default() {
        return null
      },
    },
    showId: {
      type: Boolean,
      default() {
        return false
      },
    },
    maxLength: {
      type: Number,
      default() {
        return 5
      }
    }
  },
  methods: {
    clickDeleteRelationshipButton() {
      this.$confirm('永久删除该关系?', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.loading = true
        this.show = true
        this.axios.delete('/api/relationship/' + this.relationship.id).then(() => {
          this.$notify({
            title: '成功',
            type: 'success',
            message: '删除成功',
          })
          this.loading = false
          this.$emit('relationship-deleted')
        }).catch(err => {
          this.util.errorHint(err, '删除关系失败')
          this.loading = false
        })
      }).catch(() => {
        this.show = true
      })
    },
    getDisplayInfo() {
      let res = this.showId ? '(' + this.node.id + '):' : '',
          name = this.node.properties.name
      if (!name) {
        name = '未命名'
      }
      return res + name.length > this.maxLength ? name.substring(0, 3) + '...' : name
    },
    clickEditRelationshipButton() {
      this.$emit('edit-relationship', this.relationship)
      this.show = false
    },
    clickNodeButton() {
      this.show = false
      this.bus.$emit('newTab', {
        type: 'node-view',
        node: this.node,
      })
    },
  },
}
</script>

<style scoped>
</style>