const net = require('net');

const groups = {};

const generateGameId = () => {
  let gameId = Math.floor(Math.random() * 100000000);
  while (groups[gameId]) {
    gameId = Math.floor(Math.random() * 100000000);
  }
  return gameId.toString().padStart(8, '0');
};

const server = net.createServer((socket) => {
  let gameId;

  socket.on('data', (data) => {
    const message = data.toString().trim();
    if (!gameId && message.length === 8 && /^\d+$/.test(message) && groups[message]) {
      gameId = message;
      groups[gameId].push(socket);
      console.log(`Player connected to group ${gameId}`);
      socket.write("Successful connection to game: " + gameId + "\n");
    } else if (!gameId && message === '1') {
      gameId = generateGameId();
      groups[gameId] = [socket];
      console.log(`New group ${gameId} created`);
      socket.write(gameId + "\n");
    } else if (gameId && groups[gameId]) {
      console.log(`Data received from player in group ${gameId}: ${message}`);
      groups[gameId].forEach((player) => {
        if (player !== socket) {
          player.write(message + "\n");
        }
      });
    } else {
      console.log('Invalid input or not in a group: ' + message);
    }
  });

  socket.on('end', () => {
    if (gameId && groups[gameId]) {
      const index = groups[gameId].indexOf(socket);
      if (index > -1) {
        groups[gameId].splice(index, 1);
        console.log(`Player disconnected from group ${gameId}`);
        groups[gameId].forEach((player) => {
          console.log("sent reload");
          player.write("playersreload\n");
        });
      }
      if (groups[gameId].length === 0) {
        delete groups[gameId];
        console.log(`Group ${gameId} deleted`);
      }
    }
  });

  socket.on('error', (err) => {
    if (err.code === 'ECONNRESET') {
        if (gameId && groups[gameId]) {
            const index = groups[gameId].indexOf(socket);
            if (index > -1) {
              groups[gameId].splice(index, 1);
              console.log(`Player disconnected from group ${gameId}`);
              groups[gameId].forEach((player) => {
                console.log("sent reload");
                player.write("playersreload\n");
              });
            }
            disconnected = true;
            if (groups[gameId].length === 0) {
              delete groups[gameId];
              console.log(`Group ${gameId} deleted`);
            }
          }
      } else {
  console.error('Socket error:', err);
      }
});

});

server.on('error', (err) => {
  console.error('Server error:', err);
});

server.on('close', () => {
  console.log('Server closed');
});

server.listen(10000, "0.0.0.0", () => {
  console.log('Server listening on port 10000');
});
