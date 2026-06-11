import api from './axios.js'
export const triggerSos    = (data)  => api.post('/sos/trigger', data)
export const getActiveSos  = ()      => api.get('/sos/active')
export const getSosHistory = ()      => api.get('/sos/history')
export const updateLocation= (id, data) => api.put(`/sos/${id}/location`, data)
export const resolveSos    = (id)    => api.patch(`/sos/${id}/resolve`)
export const cancelSos     = (id)    => api.patch(`/sos/${id}/cancel`)
