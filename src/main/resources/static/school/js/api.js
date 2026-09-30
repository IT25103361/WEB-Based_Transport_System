// Use this application's context path and port (8081 in application.properties).
const API_BASE_URL = (document.querySelector('meta[name="app-context"]')?.content || "") + "/api";

const Api = (() => {
  const getToken = () => localStorage.getItem("nextgo_token");
  const getParentId = () => localStorage.getItem("nextgo_parent_id");
  const unwrap = data => data?.data ?? data?.result ?? data;
  async function request(path, options = {}) {
    const headers = { Accept: "application/json", ...options.headers };
    const token = getToken(); if (token) headers.Authorization = `Bearer ${token}`;
    if (options.body && !headers["Content-Type"]) headers["Content-Type"] = "application/json";
    let response;
    try { response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers }); }
    catch { throw new Error("Unable to connect to the transport server. Please check that the backend is running."); }
    if (response.status === 401) { localStorage.removeItem("nextgo_token"); if (!location.pathname.endsWith("login")) location.href = "login"; throw new Error("Your session has expired. Please sign in again."); }
    let data = null; try { data = await response.json(); } catch { /* response may be empty */ }
    if (!response.ok) throw new Error(data?.message || data?.error || "The transport server could not complete this request.");
    return unwrap(data);
  }
  return {
    login: async (email, password) => request("/auth/login", { method:"POST", body:JSON.stringify({ email, password }) }),
    getParent: id => request(`/parents/${id || getParentId()}`),
    getStudents: id => request(`/parents/${id || getParentId()}/students`),
    getStudent: id => request(`/students/${id}`),
    getStudentTransport: id => request(`/students/${id}/transport`),
    getBuses: () => request("/buses"), getBus: id => request(`/buses/${id}`),
    getBusLocation: id => request(`/buses/${id}/location`), getBusLocationHistory: id => request(`/buses/${id}/location/history`),
    getTransportTracking: id => request(`/transport/track/${id}`), getETA: id => request(`/transport/eta/${id}`),
    getSchedules: () => request("/schedules"), getSchedule: id => request(`/schedules/${id}`),
    createSchedule: schedule => request("/schedules", {method:"POST",body:JSON.stringify(schedule)}),
    updateSchedule: (id, schedule) => request(`/schedules/${id}`, {method:"PUT",body:JSON.stringify(schedule)}),
    deleteSchedule: id => request(`/schedules/${id}`, {method:"DELETE"}),
    getNotifications: id => request(`/parents/${id || getParentId()}/notifications`),
    sendNotification: (parentId, notif) => request(`/parents/${parentId || getParentId()}/notifications`, { method:"POST", body:JSON.stringify(notif) }),
    markNotificationRead: id => request(`/notifications/${id}/read`, { method:"PUT" }),
    markAllNotificationsRead: id => request(`/parents/${id || getParentId()}/notifications/read-all`, { method:"PUT" }),
    deleteAllNotifications: id => request(`/parents/${id || getParentId()}/notifications`, { method:"DELETE" }),
    deleteNotification: id => request(`/notifications/${id}`, { method:"DELETE" }),
    getRoutes: () => request("/routes"),
    getRouteStops: routeId => request(`/routes/${routeId}/stops`),
    recordArrival: studentId => request(`/students/${studentId}/arrival`, { method: "POST" }),
    sendLocationPing: (busId, loc) => request(`/buses/${busId}/location`, { method: "POST", body: JSON.stringify(loc) }),
    updateStudentStops: (id, pickupOrPayload, dropOffStop) => {
      const body = (typeof pickupOrPayload === "object" && pickupOrPayload !== null) ? pickupOrPayload : { pickupStop: pickupOrPayload, dropOffStop };
      return request(`/students/${id}/transport-stops`, { method:"PUT", body:JSON.stringify(body) });
    },
    deleteStudentTransport: id => request(`/students/${id}/transport`, { method: "DELETE" }),
    setBusTripMode: (busId, direction) => request(`/buses/${busId}/trip-mode?direction=${direction}`, { method:"POST" }),
    getArrivalStatus: id => request(`/students/${id}/arrival`),
    logout: () => { localStorage.removeItem("nextgo_token");localStorage.removeItem("nextgo_parent_id");localStorage.removeItem("nextgo_selected_student");localStorage.removeItem("nextgo_cached_students");location.href="login"; }
  };
})();
