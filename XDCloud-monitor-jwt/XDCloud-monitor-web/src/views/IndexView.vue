<template>
  <el-container class="main-container">
    <el-header class="main-header">
      <el-image style="height: 30px"
                src="https://element-plus.org/images/element-plus-logo.svg"/>
      <div class="tabs">
        <tab-item v-for="item in tabs" :name="item.name"
                  :active="item.id === tab" @click="changePage(item)"/>
        <el-switch style="margin: 0 20px"
                   v-model="dark" active-color="#424242"
                   :active-action-icon="Moon"
                   :inactive-action-icon="Sunny"/>
        <div style="text-align: right;line-height: 16px;margin-right: 10px">
          <div>
            <el-tag type="success" v-if="store.isAdmin" size="small">管理员</el-tag>
            <el-tag v-else size="small">子账户</el-tag>
            {{store.user.username}}
          </div>
          <div style="font-size: 13px;color: grey">{{store.user.email}}</div>
        </div>
        <el-dropdown>
          <el-avatar class="avatar"
                     src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"/>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="userLogout">
                <el-icon><Back/></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <keep-alive exclude="Security">
            <component :is="Component"/>
          </keep-alive>
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { logout } from '@/net'
import router from "@/router";
import {Back, Moon, Sunny} from "@element-plus/icons-vue";
import {ref} from "vue";
import {useDark} from "@vueuse/core";
import TabItem from "@/component/TabItem.vue";
import {useRoute} from "vue-router";
import {useStore} from "@/store";

const store = useStore()

const route = useRoute()
const dark = ref(useDark())
const tabs = [
  {id: 1, name: '管理', route: 'manage'},
  {id: 2, name: '安全', route: 'security'}
]
const defaultIndex = () => {
  for (let tab of tabs) {
    if(route.name === tab.route)
      return tab.id
  }
  return 1
}
const tab = ref(defaultIndex())
function changePage(item) {
  tab.value = item.id
  router.push({name: item.route})
}

function userLogout() {
  logout(() => router.push("/"))
}
</script>

<style scoped>
.main-container {
  height: 100vh;
  width: 100vw;

  .main-header {
    height: 55px;
    background-color: var(--el-bg-color);
    border-bottom: solid 1px var(--el-border-color);
    display: flex;
    align-items: center;

    .tabs {
      height: 55px;
      gap: 10px;
      flex: 1px;
      display: flex;
      align-items: center;
      justify-content: right;
    }
  }

  .main-content {
    height: 100%;
    background-color: #f5f5f5;
  }
}

.dark .main-container .main-content {
  background-color: #232323;
}
</style>
