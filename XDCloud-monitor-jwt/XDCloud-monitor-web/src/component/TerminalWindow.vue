<script setup>
import {reactive, ref, watch} from "vue";
import {get, post} from "@/net";
import Terminal from "@/component/Terminal.vue";

const props = defineProps({
  id: Number
})

const connection = reactive({
  ip: '',
  port: 22,
  username: '',
  password: ''
})

const rules = {
  port: [
    { required: true, message: '请输入端口', trigger: ['blur', 'change'] },
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['blur', 'change'] },
  ]
}

const state = ref(1)
const formRef = ref()

function saveConnection() {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/monitor/ssh-save', {
        ...connection,
        id: props.id
      }, () => state.value = 2)
    }
  })
}

watch(() => props.id, id => {
  state.value = 1
  if(id !== -1) {
    connection.ip = ''
    get(`/api/monitor/ssh?clientId=${id}`, data => Object.assign(connection, data))
  }
}, { immediate: true })
</script>

<template>
  <div class="terminal-main">
    <div class="login" v-loading="!connection.ip" v-if="state === 1">
      <i style="font-size: 50px" class="fa-solid fa-terminal"></i>
      <div style="margin-top: 10px;font-weight: bold;font-size: 20px">服务端连接信息</div>
      <el-form style="width: 400px;margin: 20px auto" :model="connection"
               :rules="rules" ref="formRef" label-width="100">
        <div style="display: flex;gap: 10px">
          <el-form-item style="width: 100%" label="服务器IP地址" prop="ip">
            <el-input v-model="connection.ip" disabled/>
          </el-form-item>
          <el-form-item style="width: 80px" prop="port" label-width="0">
            <el-input placeholder="端口" v-model="connection.port"/>
          </el-form-item>
        </div>
        <el-form-item prop="username" label="登录用户名">
          <el-input placeholder="请输入用户名..." v-model="connection.username"/>
        </el-form-item>
        <el-form-item prop="password" label="登录密码">
          <el-input placeholder="请输入密码..." type="password" v-model="connection.password"/>
        </el-form-item>
        <el-button type="success" @click="saveConnection" plain>立即连接</el-button>
      </el-form>
    </div>
    <div v-if="state === 2">
      <div style="overflow: hidden;padding: 0 10px 10px 10px">
        <terminal :id="id" @dispose="state = 1"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.terminal-main {
  width: 100%;
  height: 100%;

  .login {
    text-align: center;
    padding-top: 50px;
    height: 100%;
    box-sizing: border-box;
  }
}
</style>
