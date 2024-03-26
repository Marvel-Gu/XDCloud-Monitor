import {useClipboard} from "@vueuse/core";
import {ElMessage, ElMessageBox} from "element-plus";
import {post} from "@/net";

function fitByUnit(value, unit) {
    const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
    let index = units.indexOf(unit)
    while (((value < 1 && value !== 0) || value >= 1024) && (index >= 0 || index < units.length)) {
        if(value >= 1024) {
            value = value / 1024
            index = index + 1
        } else {
            value = value * 1024
            index = index - 1
        }
    }
    return `${parseInt(value)} ${units[index]}`
}

function percentageToStatus(percentage) {
    if(percentage < 50)
        return 'success'
    else if(percentage < 80)
        return 'warning'
    else
        return 'exception'
}

function osNameToIcon(name) {
    if(name.indexOf('Ubuntu') >= 0)
        return {icon: 'fa-ubuntu', color: '#db4c1a'}
    else if(name.indexOf('CentOS') >= 0)
        return {icon: 'fa-centos', color: '#9dcd30'}
    else if(name.indexOf('macOS') >= 0)
        return {icon: 'fa-apple', color: 'grey'}
    else if(name.indexOf('Windows') >= 0)
        return {icon: 'fa-windows', color: '#3578b9'}
    else if(name.indexOf('Debian') >= 0)
        return {icon: 'fa-debian', color: '#a80836'}
    else
        return {icon: 'fa-linux', color: 'grey'}
}

function cpuNameToImage(name) {
    if(name.indexOf('Apple') >= 0)
        return 'Apple.png'
    else if(name.indexOf('AMD') >= 0)
        return 'AMD.png'
    else
        return 'Intel.png'
}

const { copy } = useClipboard()
const copyIp = ip => copy(ip).then(() => ElMessage.success('成功复制IP地址到剪贴板'))

function rename(id, name, after) {
    ElMessageBox.prompt('请输入新的服务器主机名称', '修改名称', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputValue: name,
        inputPattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]{1,10}$/,
        inputErrorMessage: '名称只能包含中英文字符、数字和下划线',
    }).then(({ value }) => post('/api/monitor/rename', {
            id: id,
            name: value
        }, () => {
            ElMessage.success('主机名称已更新')
            after()
        })
    )
}

export { fitByUnit, percentageToStatus, cpuNameToImage, osNameToIcon, rename, copyIp }
