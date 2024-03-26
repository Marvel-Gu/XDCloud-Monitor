<script setup>
import PreviewCard from "@/component/PreviewCard.vue";
import {computed, reactive, ref} from "vue";
import {get} from "@/net";
import ClientDetails from "@/component/ClientDetails.vue";
import RegisterCard from "@/component/RegisterCard.vue";
import {Plus} from "@element-plus/icons-vue";
import {useRoute} from "vue-router";
import {useStore} from "@/store";
import TerminalWindow from "@/component/TerminalWindow.vue";

const locations = [
  {name: 'cn', desc: '中国大陆'},
  {name: 'hk', desc: '香港'},
  {name: 'jp', desc: '日本'},
  {name: 'us', desc: '美国'},
  {name: 'sg', desc: '新加坡'},
  {name: 'kr', desc: '韩国'},
  {name: 'de', desc: '德国'}
]
const checkedNodes = ref([])

const list = ref([])
const store = useStore()

const route = useRoute()

const updateList = () => {
  if(route.name === 'manage') {
    get('/api/monitor/list', data => list.value = data)
  }
}
setInterval(updateList, 10000)
updateList()

const detail = reactive({
  show: false,
  id: -1
})
const displayClientDetails = (id) => {
  detail.show = true
  detail.id = id
}

const clientList = computed(() => {
  if(checkedNodes.value.length === 0) {
    return list.value
  } else {
    return list.value.filter(item => checkedNodes.value.indexOf(item.location) >= 0)
  }
})

const register = reactive({
  show: false,
  token: ''
})
const refreshToken = () => get('/api/monitor/register', token => register.token = token)

function openTerminal(id) {
  terminal.show = true
  terminal.id = id
  detail.show = false
}
const terminal = reactive({
  show: false,
  id: -1
})
</script>

<template>
  <div class="manage-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
      <div>
        <div class="title"><i class="fa-solid fa-server"></i> 管理主机列表</div>
        <div class="desc">在这里管理所有已经注册的主机实例，实时监控主机运行状态，快速进行管理和操作。</div>
      </div>
      <div>
        <el-button :icon="Plus" type="primary" plain :disabled="!store.isAdmin"
                   @click="register.show = true">添加新主机</el-button>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div style="margin-bottom: 20px">
      <el-checkbox-group v-model="checkedNodes">
        <el-checkbox v-for="node in locations" :key="node" :label="node.name" border>
          <span :class="`flag-icon flag-icon-${node.name}`"></span>
          <span style="font-size: 13px;margin-left: 10px">{{node.desc}}</span>
        </el-checkbox>
      </el-checkbox-group>
    </div>
    <div class="card-list" v-if="list.length">
      <preview-card v-for="item in clientList" :data="item" :update="updateList"
                    @click="displayClientDetails(item.id)"/>
    </div>
    <el-empty description="还没有任何主机哦，点击右上角添加一个吧" v-else/>
    <el-drawer size="520" :show-close="false" v-model="detail.show"
               :with-header="false" v-if="list.length" @close="detail.id = -1">
      <client-details :id="detail.id" :update="updateList" @delete="updateList" @terminal="openTerminal"/>
    </el-drawer>
    <el-drawer v-model="register.show" direction="btt" :with-header="false"
               style="width: 600px;margin: 10px auto" size="320" @open="refreshToken">
      <register-card :token="register.token"/>
    </el-drawer>
    <el-drawer style="width: 800px" :size="520" direction="btt"
               @close="terminal.id = -1"
               v-model="terminal.show" :close-on-click-modal="false">
      <template #header>
        <div>
          <div style="font-size: 18px;color: dodgerblue;font-weight: bold;">SSH远程连接</div>
          <div style="font-size: 14px">
            远程连接的建立将由服务端完成，因此在内网环境下也可以正常使用。
          </div>
        </div>
      </template>
      <terminal-window :id="terminal.id"/>
    </el-drawer>
  </div>
</template>

<style scoped>
:deep(.el-drawer__header) {
  margin-bottom: 10px;
}

:deep(.el-checkbox-group .el-checkbox) {
  margin-right: 10px;
}

:deep(.el-drawer) {
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

:deep(.el-drawer__body) {
  padding: 0;
}

.manage-main {
  margin: 0 50px;

  .title {
    font-size: 22px;
    font-weight: bold;
  }

  .desc {
    font-size: 15px;
    color: grey;
  }
}

.card-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
</style>
