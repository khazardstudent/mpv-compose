#pragma once

#include <atomic>

struct mpv_handle;

extern JavaVM *g_vm;
extern mpv_handle *g_mpv;
extern std::atomic<bool> g_event_thread_request_exit;
