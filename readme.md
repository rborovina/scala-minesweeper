# Scala Minesweeper

This is a Scala implementation of the classic Minesweeper game. The project features different difficulty levels, the ability to save and load games, hints, and a scoring system that takes into account the number of bombs, hints used, elapsed time, and user actions.

## Features

- **Different Difficulty Levels**: Beginner, Intermediate, and Hard.
- **Save and Load Games**: Players can save their progress and resume later.
- **Hints**: Players can request hints during the game.
- **Scoring System**: The score is calculated based on the number of bombs, hints used, elapsed time, and user actions.
- **Best Scores**: View a list of the best scores achieved.

## Getting Started

### Prerequisites

- Scala (2.13.x)
- sbt (1.x)

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/rborovina/scala-minesweeper.git
   cd scala-minesweeper
   ```

2. Run the project:
   ```sh
   sbt run
   ```

## Project Structure

- `actions`: Contains action-related classes and enums.
- `controller`: Contains the game logic and controller classes.
- `helpers`: Contains helper classes for file operations and game-related utilities.
- `isometrics`: Contains transformations and symmetries used in the game.
- `model`: Contains the core data models (e.g., Cell, BombCell, EmptyCell).
- `traits`: Contains trait definitions used across the project.
- `types`: Contains type aliases for commonly used types.
- `view`: Contains the GUI components and screens.

## Key Classes and Functions

### GameController

The `GameController` class manages the game state and logic. It handles user actions, calculates the game score, and logs completed games.

### FileHelper

The `FileHelper` object provides methods for saving and loading game states, as well as logging completed games.

### GameHelper

The `GameHelper` object provides utility methods, such as generating unique filenames and calculating the game score.

### Main Menu

The `MainMenuScreen` class provides the main menu interface where players can start a new game, load an existing game, create a new game level, or view the best scores.

## Scoring System

The score is calculated using the following formula:
```scala
def calculateScore(bombs: Int, hints: Int, elapsedTime: Int, userActions: Int): Int = {
  val baseScore = bombs * 10
  val hintPenalty = hints * 5
  val timePenalty = elapsedTime / 60
  val actionPenalty = userActions * 2  // Adjust the multiplier as needed

  baseScore - hintPenalty - timePenalty - actionPenalty
}
```

## Running the Tests

To run the tests, use the following command:
```sh
sbt test
```

## License

This project is licensed under the MIT License.