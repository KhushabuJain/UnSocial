import api from './axios.js'
export const startTracking  = (data)      => api.post('/tracking/start', data)
export const updateLocation = (id, data)  => api.put(`/tracking/${id}/location`, data)
export const stopTracking   = (id)        => api.patch(`/tracking/${id}/stop`)
export const getActiveSession = ()        => api.get('/tracking/active')
export const getHistory       = ()        => api.get('/tracking/history')
