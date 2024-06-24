package com.arbr.engine.util

import com.arbr.content_formats.code.LenientCodeParser
import com.arbr.content_formats.mapper.Mappers
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LenientCodeParserTest {

    private val badCodeString = "```jsx\n" +
            "import { useState } from 'react'\n" +
            "import viteLogo from '/vite.svg'\n" +
            "import './App.css'\n" +
            "import { generateBoard } from './utils.js'\n" +
            "\n" +
            "function App() {\n" +
            "    const [difficulty, setDifficulty] = useState('easy');\n" +
            "    const [board, setBoard] = useState(generateBoard(difficulty))\n" +
            "    const [gameOver, setGameOver] = useState(false);\n" +
            "\n" +
            "    const handleDifficultyChange = (event) => {\n" +
            "        console.log(\"Hello \" + event.target.value);\n" +
            "        setDifficulty(event.target.value);\n" +
            "        let bboard = generateBoard(event.target.value)\n" +
            "        setBoard(bboard);\n" +
            "        setGameOver(false);\n" +
            "    }\n" +
            "\n" +
            "    const revealCell = (rowIndex, cellIndex) => {\n" +
            "        if (gameOver || board[rowIndex][cellIndex].isRevealed) {\n" +
            "            return;\n" +
            "        }\n" +
            "\n" +
            "        const newBoard = [...board];\n" +
            "        newBoard[rowIndex][cellIndex].isRevealed = true;\n" +
            "        setBoard(newBoard);\n" +
            "\n" +
            "        if (newBoard[rowIndex][cellIndex].isMine) {\n" +
            "            setGameOver(true);\n" +
            "        } else {\n" +
            "            checkGameOver(newBoard);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    const markMine = (rowIndex, cellIndex) => {\n" +
            "        if (gameOver || board[rowIndex][cellIndex].isRevealed) {\n" +
            "            return;\n" +
            "        }\n" +
            "\n" +
            "        const newBoard = [...board];\n" +
            "        newBoard[rowIndex][cellIndex].isMine = !newBoard[rowIndex][cellIndex].isMine;\n" +
            "        setBoard(newBoard);\n" +
            "    }\n" +
            "\n" +
            "    const checkGameOver = (board) => {\n" +
            "        for (let row of board) {\n" +
            "            for (let cell of row) {\n" +
            "                if (!cell.isMine && !cell.isRevealed) {\n" +
            "                    return;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        setGameOver(true);\n" +
            "    }\n" +
            "\n" +
            "    return (\n" +
            "        <>\n" +
            "            <div>\n" +
            "                {/*Placeholder logo. TODO: Replace or remove*/}\n" +
            "                <img src={viteLogo} className=\"logo\" alt=\"Vite logo\" />\n" +
            "            </div>\n" +
            "            <h1>Minesweeper</h1>\n" +
            "            <div>\n" +
            "                <label htmlFor=\"difficulty\">Choose a difficulty:</label>\n" +
            "                <select id=\"difficulty\" name=\"difficulty\" onChange={handleDifficultyChange}>\n" +
            "                    <option value=\"easy\">Easy</option>\n" +
            "                    <option value=\"medium\">Medium</option>\n" +
            "                    <option value=\"hard\">Hard</option>\n" +
            "                </select>\n" +
            "            </div>\n" +
            "            <div className=\"game-board\">\n" +
            "                {/*Render the game board*/}\n" +
            "                {board.map((row, rowIndex) => (\n" +
            "                    <div key={rowIndex} className=\"game-board-row\">\n" +
            "                        {row.map((cell, cellIndex) => (\n" +
            "                            <button key={cellIndex} className={`game-board-cell \${cell.isRevealed ? (cell.isMine ? 'cell-mine' : 'cell-revealed') : 'cell-hidden'}`} onClick={() => revealCell(rowIndex, cellIndex)} onContextMenu={(e) => {e.preventDefault(); markMine(rowIndex, cellIndex);}}>\n" +
            "                                {cell.isRevealed ? (cell.isMine ? 'M' : cell.adjacentMines) : 'O'}\n" +
            "                            </button>\n" +
            "                        ))}\n" +
            "                    </div>\n" +
            "                ))}\n" +
            "            </div>\n" +
            "            {gameOver && <div>Game Over</div>}\n" +
            "        </>\n" +
            "    )\n" +
            "}\n" +
            "\n" +
            "export default App\n" +
            "```\n" +
            "The syntax error was due to a missing semicolon at the end of the `onContextMenu` event handler in the button element. The semicolon has been added to correct the syntax error."

    @Test
    fun parse() {
        val res = LenientCodeParser.parse(badCodeString)
        Assertions.assertTrue(res.startsWith("import { useState }"))
        Assertions.assertTrue(res.endsWith("export default App\n"))
    }

    private val leadingEscapedBlock = """
        ```yaml
        "0-0": []
        "1-0": ["0-0"]
        "1-1": ["0-0"]
        "1-2": ["0-0"]
        "1-3": ["0-0"]
        "2-0": ["2-1", "2-2", "2-3", "2-4"]
        "2-1": []
        "2-2": []
        "2-3": []
        "2-4": []
        "3-0": ["0-0"]
        "3-1": ["3-0"]
        "4-0": []
        "5-0": ["4-0"]
        "5-1": ["4-0"]
        "5-2": ["4-0"]
        "5-3": ["4-0"]
        ```

        In this output, each operation ID is followed by a list of its dependencies. For example, the operation with ID "1-0" (which adds the "addition" function to "calculator.js") is dependent on the operation with ID "0-0" (which creates the "calculator.js" file). Similarly, the operation with ID "2-0" (which adds the "calculate" function to "src/calculator.js") is dependent on the operations with IDs "2-1", "2-2", "2-3", and "2-4" (which add the "add", "subtract", "multiply", and "divide" functions to "src/calculator.js", respectively).
    """.trimIndent()

    @Test
    fun `parses leading escaped block`() {
        val yamlMapper = Mappers.yamlMapper
        val parsed = LenientCodeParser.parse(leadingEscapedBlock)
        val parsedMap: LinkedHashMap<String, Any> = yamlMapper.readValue(parsed, jacksonTypeRef())
        Assertions.assertEquals(17, parsedMap.size)
    }
}