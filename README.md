# LightEmAll Game 💡

**LightEmAll** is a puzzle game where players rotate tiles to light up all the game pieces, starting from the power station located at the top-left corner. The goal is to connect all the tiles with minimal clicks while avoiding unnecessary moves.

---
## How It Works
- The LightEmAll game uses **Kruskal's algorithm** to create the **minimum spanning tree** (MST) that connects all tiles efficiently.
- Tiles are represented by the GamePiece class, which contains information about their position, whether they are powered, and their connection status.
- The game board is an array of these GamePiece objects.
- Tiles can be rotated to adjust their connectivity, and the game uses **breadth-first search** (BFS) to determine whether all tiles are lit.
  
## Features
- **Randomized Board**: The board layout and the connections between game pieces are randomized at the start.
- **Bias Options**: Players can choose between different wiring biases (horizontal, vertical, or normal) for varied gameplay.
- **Rotation Mechanics**: Game pieces can be rotated to change their orientation and connections.
- **Timer & Click Counter**: The game tracks the amount of time passed and the number of rotations made.
- **Minimum Spanning Tree**: The game automatically connects pieces using Kruskal’s algorithm to ensure all pieces are connected efficiently.
- **Win Condition**: The game ends when all pieces are powered.

## Gameplay Instructions
1. **Start the Game**: The game starts with a randomized board and a power station at the top-left.
2. **Rotate Tiles**: Click on a tile to rotate it and change its connections. The objective is to light up all pieces from the power station.
3. **Use Power Station**: The power station sends power to surrounding tiles. Your goal is to rotate the tiles to create paths that connect to all other tiles on the board.
4. **Timer & Clicks**: Monitor the timer and the click counter to see how long it takes and how many moves you've made.
5. **Winning**: The game is won when all tiles are powered.
6. **Restart**: Option to restart the game by pressing r



https://github.com/user-attachments/assets/af2d73fe-c266-486c-b5f9-15960551f2cf
