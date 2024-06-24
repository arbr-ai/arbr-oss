package com.arbr.alignable.helper.diff

import com.arbr.content_formats.format.DiffLiteralPatchSectionSerializer
import com.arbr.content_formats.format.DiffLiteralSourceDocumentSerializer
import com.arbr.content_formats.format.DiffParsedPatchSection
import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.arbr.model_suite.predictive_models.document_diff_alignment.SimpleTextDiffAlignmentHelper
import com.arbr.ml.optimization.base.GradientDescentEvaluator
import com.arbr.ml.optimization.gradient.GradientDescentOptimizer
import com.arbr.ml.optimization.model.GradientDescentEvaluation
import com.arbr.ml.optimization.model.ParameterAdmissibleRanges
import com.arbr.ml.optimization.model.ParameterIntervalResult
import org.apache.commons.text.similarity.LevenshteinDistance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.math.exp

class SimpleTextDiffAlignmentHelperTest {

    @Test
    fun aligns() {
        val targetContent = """
            fun hello(): String {
                println("Welcome")
                println("to")
                println("heck")

                return "yes"
            }
        """.trimIndent()

        val baseContent = """
            fun hello() {
                println("Welcome")
                println("to")
                println("the")
                println("unit")
                println("test")
            }
        """.trimIndent()

        val alignmentHelper = SimpleTextDiffAlignmentHelper.default()
        val alignment = alignmentHelper.align(
            targetContent,
            baseContent,
        ).block()!!

        assertNotNull(alignment)

        for (op in alignment) {
            println(op)
        }

        val section = DiffParsedPatchSection(
            lineStart = null,
            lineEnd = null,
            targetLineStart = null,
            targetLineEnd = null,
            operations = LinearOrderList(alignment).map { it.diffOperation },
        )
        val literalPatch = DiffLiteralPatchSectionSerializer().serialize(LinearOrderList(listOf(section)))

        println()
        println(literalPatch.text)

        val serializedDoc = DiffLiteralSourceDocumentSerializer().serialize(
            LinearOrderList(alignment).map { it.diffOperation }
        )
        println("===== Flat applied diff: =====")
        println(serializedDoc.text)
        Assertions.assertEquals(targetContent, serializedDoc.text)
    }

    @Test
    fun `aligns 2`() {
        val baseContent = """
            import React from 'react';
            import {useState, useEffect} from 'react';
            import './WeatherDisplay.css'; // Assuming a CSS file for styling

            const WeatherDisplay = ({weatherData}) => {
                if (!weatherData || isLoading) {
                    return <div>Loading weather data...</div>;
                }

                const {currentWeather, forecast} = weatherData;

                return (
                    <div className="weather-display">
                        <div className="current-weather">
                            <h2>Current Weather</h2>
                            <p>Temperature: {currentWeather.temperature}°C</p>
                            <p>Condition: {currentWeather.condition}</p>
                        </div>
                        <div className="forecast">
                            <h2>5-Day Forecast</h2>
                            {forecast.map((day, index) => (
                                <div key={index} className="forecast-day">
                                    <p>Date: {day.date}</p>
                                    <p>Temperature: {day.temperature}°C</p>
                                    <p>Condition: {day.condition}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                );
            };

            export default WeatherDisplay;
        """.trimIndent()

        val targetContent = """
            import React from 'react';
            import {useState, useEffect} from 'react';
            import './WeatherDisplay.css'; // Assuming a CSS file for styling

            const WeatherDisplay = () => {
                const [weatherData, setWeatherData] = useState(null);
                const [isLoading, setIsLoading] = useState(true);

                useEffect(() => {
                    const fetchWeatherData = async () => {
                        setIsLoading(true);
                        try {
                            const response = await fetch('https://api.weatherapi.com/v1/forecast.json?key=YOUR_API_KEY&q=YOUR_LOCATION&days=5');
                            if (!response.ok) {
                                throw new Error('Weather data fetch failed');
                            }
                            const data = await response.json();
                            setWeatherData({
                                currentWeather: {
                                    temperature: data.current.temp_c,
                                    condition: data.current.condition.text,
                                },
                                forecast: data.forecast.forecastday.map(day => ({
                                    date: day.date,
                                    temperature: day.day.avgtemp_c,
                                    condition: day.day.condition.text,
                                })),
                            });
                        } catch (error) {
                            console.error(error);
                        } finally {
                            setIsLoading(false);
                        }
                    };

                    fetchWeatherData();
                }, []);

                if (!weatherData) {
                    return <div>Loading weather data...</div>;
                }

                const {currentWeather, forecast} = weatherData;

                return (
                    <div className="weather-display">
                        <div className="current-weather">
                            <h2>Current Weather</h2>
                            <p>Temperature: {currentWeather.temperature}°C</p>
                            <p>Condition: {currentWeather.condition}</p>
                        </div>
                        <div className="forecast">
                            <h2>5-Day Forecast</h2>
                            {forecast.map((day, index) => (
                                <div key={index} className="forecast-day">
                                    <p>Date: {day.date}</p>
                                    <p>Temperature: {day.temperature}°C</p>
                                    <p>Condition: {day.condition}</p>
                                </div>
                            ))}
                        </div>
                    </div>
                );
            };

            export default WeatherDisplay;
        """.trimIndent()

        val levenshteinDistance = LevenshteinDistance.getDefaultInstance()
        val defaultAlignmentHelper = SimpleTextDiffAlignmentHelper.default()
        val defaultParameterMap = defaultAlignmentHelper.parameterValueProvider.getParameterMap()
        val parameterRanges = defaultParameterMap.map { (_, bindingParameter) ->
            ParameterAdmissibleRanges(
                bindingParameter,
                score = null,
                ranges = listOf(ParameterIntervalResult(0.0, 1E14))
            )
        }

        val evaluator = GradientDescentEvaluator { paramMaps ->
                Flux.fromIterable(paramMaps.withIndex())
                    .concatMap { (idx, paramMap) ->
                        val alignmentHelper = SimpleTextDiffAlignmentHelper.withParameterMap(paramMap)

                        alignmentHelper.align(
                            targetContent,
                            baseContent,
                        ).map { alignment ->
                            val section = DiffParsedPatchSection(
                                lineStart = null,
                                lineEnd = null,
                                targetLineStart = null,
                                targetLineEnd = null,
                                operations = LinearOrderList(alignment).map { it.diffOperation },
                            )
                            val literalPatch =
                                DiffLiteralPatchSectionSerializer().serialize(LinearOrderList(listOf(section)))

                            val serializedDoc = DiffLiteralSourceDocumentSerializer().serialize(
                                LinearOrderList(alignment).map { it.diffOperation }
                            )

                            val stringDistance = levenshteinDistance.apply(
                                targetContent,
                                serializedDoc.text,
                            )
                            val score = exp(-0.01 * stringDistance)

                            idx to GradientDescentEvaluation(1 - score)
                        }
                    }
            }

        val resultParams = GradientDescentOptimizer(
            "test",
            parameterRanges,
            evaluator,
            {
                println("Got parameter set with score ${it.trainingScore} , ${it.testScore}: ${it.parameters}")
                Mono.empty()
            }
        ).optimize().block()

        assertNotNull(resultParams)

        run {
            val finalAlignmentHelper = SimpleTextDiffAlignmentHelper.withParameterMap(resultParams!!)
            val alignment = finalAlignmentHelper.align(
                targetContent,
                baseContent,
            ).block()!!

            for (op in alignment) {
                println(op)
            }

            val section = DiffParsedPatchSection(
                lineStart = null,
                lineEnd = null,
                targetLineStart = null,
                targetLineEnd = null,
                operations = LinearOrderList(alignment).map { it.diffOperation },
            )
            val literalPatch = DiffLiteralPatchSectionSerializer().serialize(LinearOrderList(listOf(section)))

            println()
            println(literalPatch.text)

            val serializedDoc = DiffLiteralSourceDocumentSerializer().serialize(
                LinearOrderList(alignment).map { it.diffOperation }
            )
            println("===== Flat applied diff: =====")
            println(serializedDoc.text)
            val targetLines = targetContent.split("\n")
            val serializedLines = serializedDoc.text.split("\n")
            Assertions.assertEquals(targetLines.size, serializedLines.size)

            for ((l0, l1) in targetLines.zip(serializedLines)) {
                Assertions.assertEquals(l0.trim(), l1.trim())
            }
        }
    }

    @Test
    fun `aligns 3`() {
        val dropLast = 0

        val baseContent = """
            import React from 'react';
            import {useState, useEffect} from 'react';
            import './WeatherDisplay.css'; // Assuming a CSS file for styling

            const WeatherDisplay = ({weatherData}) => {
                if (!weatherData || isLoading) {
                    return <div>Loading weather data...</div>;
                }

                const {currentWeather, forecast} = weatherData;
            };

            export default WeatherDisplay;
        """.trimIndent().dropLast(dropLast)

        val targetContent = """
            import React from 'react';
            import {useState, useEffect} from 'react';
            import './WeatherDisplay.css'; // Assuming a CSS file for styling

            const WeatherDisplay = ({weatherData}) => {
                if (!weatherData || isLoading) {
                    return <div>Loading weather data...</div>;
                }

                useEffect(() => {
                  const fetchWeatherData = async () => {
                        setIsLoading(true);
                     try {
                     } catch (error) {
                        console.error(error);
                     } finally {
                        setIsLoading(false);
                     }
                  };

                     fetchWeatherData();
                }, []);

                const {currentWeather, forecast} = weatherData;
            };

            export default WeatherDisplay;
        """.trimIndent().dropLast(dropLast)

        val alignmentHelper = SimpleTextDiffAlignmentHelper.default()
        val alignment = alignmentHelper.align(
            targetContent,
            baseContent,
        ).block()!!

        for (op in alignment) {
            println(op)
        }

        val section = DiffParsedPatchSection(
            lineStart = null,
            lineEnd = null,
            targetLineStart = null,
            targetLineEnd = null,
            operations = LinearOrderList(alignment).map { it.diffOperation },
        )
        val literalPatch = DiffLiteralPatchSectionSerializer().serialize(LinearOrderList(listOf(section)))

        println()
        println(literalPatch.text)

        val serializedDoc = DiffLiteralSourceDocumentSerializer().serialize(
            LinearOrderList(alignment).map { it.diffOperation }
        )
        println("===== Flat applied diff: =====")
        println(serializedDoc.text)
        val targetLines = targetContent.split("\n")
        val serializedLines = serializedDoc.text.split("\n")
        Assertions.assertEquals(targetLines.size, serializedLines.size)

        for ((l0, l1) in targetLines.zip(serializedLines)) {
            Assertions.assertEquals(l0.trim(), l1.trim())
        }
    }

}
