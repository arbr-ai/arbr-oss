package com.arbr.engine.util

import com.arbr.api.workflow.view_model.WorkflowViewModelFileLineAnnotationKind
import org.junit.jupiter.api.Test

class ViewModelDiffUtilsTest {

    @Test
    fun `computes line-based diff`() {
        val baseContent = """
            fun hello() {
                println("Welcome")
                println("to")
                println("the")
                println("unit")
                println("test")
            }
        """.trimIndent()

        val targetContent = """
            fun hello(): String {
                println("Welcome")
                println("to")
                println("heck")
                
                return "yes"
            }
        """.trimIndent()

        val fileData = ViewModelDiffUtils
            .getViewModelFileData(
                "Main.kt",
                "src/Main.kt",
                baseContent,
                targetContent,
            )
            .block()!!

        println(fileData.fileContents)
        println()
        val added = fileData.annotations.filter { it.annotationKind == WorkflowViewModelFileLineAnnotationKind.DIFF_ADDED }
            .map { it.lineIndex }
        val deleted = fileData.annotations.filter { it.annotationKind == WorkflowViewModelFileLineAnnotationKind.DIFF_DELETED }
            .map { it.lineIndex }
        println("Added: $added")
        println("Deleted: $deleted")
    }

    @Test
    fun `computes long line-based diff`() {
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
            import { useState, useEffect } from 'react';
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
            
              const { currentWeather, forecast } = weatherData;
            
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

        val fileData = ViewModelDiffUtils
            .getViewModelFileData(
                "Main.kt",
                "src/Main.kt",
                baseContent,
                targetContent,
            )
            .block()!!

        println(fileData.fileContents)
        println()
        val added = fileData.annotations.filter { it.annotationKind == WorkflowViewModelFileLineAnnotationKind.DIFF_ADDED }
            .map { it.lineIndex }
        val deleted = fileData.annotations.filter { it.annotationKind == WorkflowViewModelFileLineAnnotationKind.DIFF_DELETED }
            .map { it.lineIndex }
        println("Added: $added")
        println("Deleted: $deleted")
    }

}