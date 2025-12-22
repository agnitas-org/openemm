AGN.Lib.DomInitializer.new('server-sent-events', function () {
  // TODO add some comments explaining why we need BroadcastChannel and Web Lock usage after GWUA-6572 has been successfully tested

  if (!('locks' in navigator)) {
    console.warn('Web Locks API not supported.');
    return;
  }

  /*
   * One browser window may contain several logged-in users when using URL-based session tracking.
   * Admin ID in BroadcastChannel and Web Locks helps coordinate tabs of the same user without affecting others.
   */
  const channel = new BroadcastChannel(`sse-leader-channel-${window.adminId}`);
  let eventSource = null;
  let isLeader = false;

  function handleEventSourceEvents() {
    eventSource = new EventSource(AGN.url('/sse/connect.action'));

    eventSource.addEventListener('notification', (event) => {
      channel.postMessage({ type: 'notification', popupsJson: event.data }); // leader tab broadcasts data to other tabs
      showPopupsAndAcknowledgeServer(event.data);
    });
    eventSource.addEventListener('debug', (event) => {
      console.info('SSE init. Status: ' + event.data);
    });

    eventSource.onerror = (err) => {
      console.error('Error during server send events:', err);
      eventSource.close();

      if (eventSource.readyState === EventSource.CLOSED) {
        return;
      }

      setTimeout(() => {
        if (isLeader) { // attempt to reconnect if this tab still the leader
          handleEventSourceEvents();
        }
      }, 5000);
    };
  }

  function tryBecomeLeader() {
    navigator.locks.request(`sse-leader-lock-${window.adminId}`, { mode: 'exclusive' }, (lock) => {
      if (!lock) {
        return; // this tab failed to get the lock, so it's a follower tab
      }
      isLeader = true; // this tab got the lock -> it's become leader
      handleEventSourceEvents();
      return new Promise(() => {}); // hold the leader lock
    }).catch(err => console.error('Error acquiring leader lock:', err));
  }

  function showPopupsAndAcknowledgeServer(popupsStr) {
    AGN.Lib.JsonMessages(JSON.parse(popupsStr));
    $.post(AGN.url(`/sse/ack.action`)); // respond server that popups have been shown
  }

  // Follower tabs will listen for messages from the leader and also show them
  channel.addEventListener('message', (event) => {
    if (event.data.type === 'notification') {
      showPopupsAndAcknowledgeServer(event.data.popupsJson);
    }
  });

  tryBecomeLeader();
});
