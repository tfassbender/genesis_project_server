# genesis_project_server

A simple game server for the game [GenesisProject](https://github.com/tfassbender/genesis_project).  
The server is used to store the user data, games and configuration.

Storing and loading of user data, games or configuration can be done using REST.


## Service methods

All resources can be accessed using a URI beginning with:  
[the_host_name]:[the_port__default_is_5715]/genesis_project_server/genesis_project/genesis_project/**[method_and_parameters]**

A short description of all provided methods:

- **.../hello**: Just answers with a 'hello' message to test whether the server is running

- **.../test_db**: Tests whether the database is running and reachable. 
         
- **../update_game**: Updates a game in the database to the latest game state
        
- **../get_game/{id}**: Requests the current state of a game in the database

- **../get_config/{config}**: Requests a configuration file from the server
        
- **../set_move**: Sets a move that was made in a game
        
- **../create_game**: Creates a new game
        
- **../create_user**: Creates a new user
        
- **../update_user**: Updates a user's name or password
        
- **../verify_user**: Authenticates a user
        
- **../list_games/{complete}/{username}**: Lists all games (or all games of a user)

- **../list_moves/{game_id}/{username}/{num_moves}**: Lists the moves of a game or a user

- **../reset_test_database**: Resets the test database to (re-)create a clean testing environment (by dropping and re-building the complete test database)

A detailed description of all methods:

- **.../hello**: Just answers with a 'hello' message to test whether the server is running

- **.../test_db**: 
    - Tests whether the database is running and reachable. 
    - **returns**:
        - If the database is reachable returns the message 'Database up and running'
        - If the database is NOT reachable returns 'Database error: ' followed by a stacktrace that explains the error
         
- **../update_game**:
    - Updates a game in the database to the latest game state
    - **parameters**:
        - id: the id of the game in the database (as HTTP Header)
        - game: the game content as a JSON text (as HTTP POST entity)
    - **returns**:
        - HTTP 200 (OK) if the request was successful
        - HTTP 404 (NOT_FOUND) if the game id wasn't found in the database
        - HTTP 500 (INTERNAL_SERVER_ERROR) if some unexpected error occurs
        
- **../get_game/{id}**: 
    - Requests the current state of a game in the database
    - **parameters**:
        - id: the id of the game in the database
    - **returns**:
        - The game content as a JSON text
        
- **../get_config/{config}**: 
    - Requests a configuration file from the server
    - **parameters**:
        - config: the configuration file that is requested (allowed by default should be: 'constants', 'description_texts' and 'main_menu_dynamic_content')
    - **returns**:
        - The content of the configuration file (JSON or TXT)
        
- **../set_move**:
    - Sets a move that was made in a game
    - **parameters**:
        - game_id: the id of the game in the database (as HTTP Header)
        - username: the name of the user who made the move (as HTTP Header)
        - move: the move content as JSON text (as HTTP POST entity)
    - **returns**:
        - HTTP 200 (OK) if the request was successful
        - HTTP 404 (NOT_FOUND) if the game id or the username wasn't found in the database
        - HTTP 500 (INTERNAL_SERVER_ERROR) if some unexpected error occurs
        
- **../create_game**: 
    - Creates a new game
    - **parameters (HTTP POST)**:
        - A list of strings (serialized as JSON) that contains the usernames of all players that take part in the game
    - **returns**:
        - The id of the game in the database
        
- **../create_user**: 
    - Creates a new user
    - **parameters (HTTP POST)**:
        - A [Login](https://github.com/tfassbender/genesis_project_server/blob/master/src/main/java/net/jfabricationgames/genesis_project_server/user/Login.java) object that contains the username and password of the new player
    - **returns**:
        - HTTP 200 (OK) if the request was successful
        - HTTP 403 (FORBIDDEN) if a user with this username already exists
        - HTTP 500 (INTERNAL_SERVER_ERROR) if some unexpected error occurs
        
- **../update_user**: 
    - Updates a user's name or password
    - **parameters**:
        - A list of (two) [Login](https://github.com/tfassbender/genesis_project_server/blob/master/src/main/java/net/jfabricationgames/genesis_project_server/user/Login.java) objects. Where the first one is the current user authentication and the second one is the updated user authentication
    - **returns**:
        - HTTP 200 (OK) if the request was successful
        - HTTP 400 (BAD_REQUEST) if the parameter contained only one login
        - HTTP 403 (FORBIDDEN) if the user validation failed
        - HTTP 404 (NOT_FOUND) if a user with the updated username already exists
        - HTTP 500 (INTERNAL_SERVER_ERROR) if some unexpected error occurs
        
- **../verify_user**: 
    - Authenticates a user
    - **parameters**:
        - A [Login](https://github.com/tfassbender/genesis_project_server/blob/master/src/main/java/net/jfabricationgames/genesis_project_server/user/Login.java) object that contains the username and password of the player that wants to authenticate
    - **returns**:
        - HTTP 200 (OK) if the request was successful and the user authentication is correct
        - HTTP 403 (FORBIDDEN) if the user validation failed
        - HTTP 404 (NOT_FOUND) if the user couldn't be found in the database
        - HTTP 500 (INTERNAL_SERVER_ERROR) if some unexpected error occurs
        
- **../list_games/{complete}/{username}**: 
    - Lists all games (or all games of a user)
    - **parameters**:
        - complete: a boolean that specifies whether the complete game content shall be loaded (complete = true) or only the ids are needed (complete = false)
        - username: the name of the user, whose games are requested (or '-' for the games of all users)
    - **returns**:
        - A [GameList](https://github.com/tfassbender/genesis_project_server/blob/master/src/main/java/net/jfabricationgames/genesis_project_server/game/GameList.java) object that contains the information about the requested games
        
- **../list_moves/{game_id}/{username}/{num_moves}**: 
    - Lists the moves of a game or a user
    - **parameters**:
        - game_id: the id of the game from which the moves should be searched (-1 for moves of all games)
        - username: the name of the user from which the moves should be searched ('-' for all users of the game)
        - num_moves: the number of moves that should be searched (last made moves first (so 2 will list the last 2 moves made in the game); -1 for all moves of the game and/or user)
    - **returns**:
        - A [MoveList](https://github.com/tfassbender/genesis_project_server/blob/master/src/main/java/net/jfabricationgames/genesis_project_server/game/MoveList.java) object that contains the moves as JSON texts
        
- **../reset_test_database**: 
    - Resets the test database to (re-)create a clean testing environment (by dropping and re-building the complete test database)
    - **parameters**:
        - NONE
    - **returns**:
        - HTTP 200 (OK) if the request was successful and the test database was reset
        - HTTP 403 (FORBIDDEN) if the current environment is not a testing environment
        - HTTP 500 (INTERNAL_SERVER_ERROR) if some unexpected error occurs