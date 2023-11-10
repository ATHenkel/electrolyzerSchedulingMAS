# Decentralized ADMM Scheduling Model for Modular Electrolysis Plants

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Introduction

This repository contains the implementation of a decentralized *Alternating Direction Method of Multipliers* (ADMM) scheduling model for optimizing the operation of modular electrolysis plants. The model is designed to balance hydrogen production with fluctuating demand, minimizing the *marginal levelized cost of hydrogen* (mLCOH) and ensuring adaptability to operational challenges.

## Getting Started

To set up and run the project, follow these steps:

### Prerequisites

The *Multi-Agent-System* (MAS) was developed using the *Java Agent Development Framework* (JADE). Agent.Workbench is used to start the MAS. 
Detailed instructions for setting up Agent.Workbench are available here:
- [Agent.Workbench](https://enflexit.gitbook.io/agent-workbench/)

### Installation of .jar-Files

1. Download the required .jar files from the xxxxx

2. Create a `lib` directory in your project root if it doesn't already exist.

3. Copy the downloaded .jar files into the `lib` directory.

4. In your Java IDE, right-click on the project and select "Properties."

5. Navigate to the "Java Build Path" or similar settings.

6. Add the .jar files from the `lib` directory to the project's build path.

### Installation of the MAS

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/YourUsername/YourRepository.git

## Features

- **Decentralized Scheduling:** The model employs a decentralized ADMM approach for scheduling, allowing for efficient and adaptive operation of modular electrolysis plants.
- **Optimization:** The model minimizes the levelized cost of hydrogen (LCOH) while meeting demand and considering various operational constraints.
- **Scalability:** Demonstrates scalability by adapting to an increasing number of electrolysis PEAs without manual configuration effort.

![grafik](https://github.com/ATHenkel/electrolyzerSchedulingMAS/assets/99994741/a60c3134-d71d-47e2-88f2-0fea9e6ef5e6)


