-- ============================================================
-- Placement Preparation Tracker - Database Setup Script
-- Run this in MySQL before starting the application
-- ============================================================

-- Step 1: Create the database
CREATE DATABASE IF NOT EXISTS placement_tracker;
USE placement_tracker;

-- Step 2: Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,          -- stores hashed password
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 3: Create problems table
CREATE TABLE IF NOT EXISTS problems (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    problem_name VARCHAR(255) NOT NULL,
    platform VARCHAR(100) NOT NULL,          -- LeetCode, HackerRank, etc.
    difficulty ENUM('Easy', 'Medium', 'Hard') NOT NULL,
    status ENUM('Solved', 'Attempted', 'Todo') DEFAULT 'Todo',
    company VARCHAR(150),                    -- e.g. Google, Amazon
    is_favorite TINYINT(1) DEFAULT 0,        -- 1 = starred, 0 = not
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 4: Create notes table
CREATE TABLE IF NOT EXISTS notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 5: Create daily_streak table (optional advanced feature)
CREATE TABLE IF NOT EXISTS daily_streak (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    solve_date DATE NOT NULL,
    problems_solved INT DEFAULT 1,
    UNIQUE KEY unique_user_date (user_id, solve_date),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- Sample Data for Testing
-- ============================================================

-- Sample user (password is 'password123' - will be hashed in real app)
-- For testing, we insert a plain record; the app will handle hashing
INSERT INTO users (name, email, password) VALUES 
('Demo Student', 'demo@test.com', 'password123');

-- Sample problems for user_id = 1
INSERT INTO problems (user_id, problem_name, platform, difficulty, status, company, is_favorite) VALUES
(1, 'Two Sum', 'LeetCode', 'Easy', 'Solved', 'Google', 1),
(1, 'Longest Substring Without Repeating Characters', 'LeetCode', 'Medium', 'Solved', 'Amazon', 0),
(1, 'Merge K Sorted Lists', 'LeetCode', 'Hard', 'Attempted', 'Microsoft', 1),
(1, 'Valid Parentheses', 'LeetCode', 'Easy', 'Solved', 'Facebook', 0),
(1, 'Binary Search', 'HackerRank', 'Easy', 'Solved', 'Apple', 0),
(1, 'Dynamic Programming Grid', 'CodeChef', 'Hard', 'Todo', 'Netflix', 0),
(1, 'Graph BFS Traversal', 'GeeksForGeeks', 'Medium', 'Solved', 'Google', 0),
(1, 'Coin Change Problem', 'LeetCode', 'Medium', 'Attempted', 'Amazon', 1),
(1, 'Rotate Matrix', 'LeetCode', 'Medium', 'Solved', 'Microsoft', 0),
(1, 'Word Search', 'LeetCode', 'Medium', 'Todo', 'Adobe', 0);

-- Sample notes for user_id = 1
INSERT INTO notes (user_id, title, content) VALUES
(1, 'Array Techniques', 'Key approaches: Two Pointers, Sliding Window, Prefix Sum.\n\nTwo Pointers: Use when array is sorted or you need pairs.\nSliding Window: Use for subarray/substring problems.\nPrefix Sum: Use for range sum queries.'),
(1, 'Graph Algorithms', 'BFS: Level-order traversal, shortest path in unweighted graphs.\nDFS: Topological sort, cycle detection.\nDijkstra: Shortest path in weighted graphs.\nUnion-Find: Connected components.'),
(1, 'Interview Tips', '1. Always clarify the problem before coding.\n2. Think out loud - communicate your approach.\n3. Start with brute force, then optimize.\n4. Check edge cases: empty input, single element, negatives.\n5. Analyze time and space complexity.');

-- Sample streak data
INSERT INTO daily_streak (user_id, solve_date, problems_solved) VALUES
(1, CURDATE() - INTERVAL 6 DAY, 2),
(1, CURDATE() - INTERVAL 5 DAY, 3),
(1, CURDATE() - INTERVAL 4 DAY, 1),
(1, CURDATE() - INTERVAL 3 DAY, 4),
(1, CURDATE() - INTERVAL 2 DAY, 2),
(1, CURDATE() - INTERVAL 1 DAY, 3),
(1, CURDATE(), 1);

-- Verification queries
SELECT 'Database setup complete!' AS status;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_problems FROM problems;
SELECT COUNT(*) AS total_notes FROM notes;
