# OpenWear

**An open-source, privacy-first companion platform for smartwatches, smart rings, and wearable devices**

OpenWear is being built around a simple idea:

>**Your body. Your data. Your device.**

The goal is to create an open, local-first alternative to proprietary wearable ecosystems. OpenWear should let people use their wearable devices without requiring an account, mandatory cloud services, advertising, or the sale of personal data.

The project starts with **Android and Polar devices**, beginning with the **Polar Pacer**, and is designed to expand to other watches, smart rings, and eventually open wearable hardware.

--

##🎯Project Goals

-🔓**Open source**
-🔒**Privacy-first**
-📴**Local-first and offline-capable**
-🚫**No selling personal or health data**
-🚫**No mandatory account**
-🚫**No mandatory cloud service**
-📦**User-controlled data export**
-🗑️**User-controlled data deletion**
-🔌**Extensible device architecture**
-⌚**Support smartwatches and smart rings**
-⚒️**Eventually support open wearable hardware and firmware**

--

##🏗️Architecture

OpenWear separates device-specific communication from the rest of the application.

#ADD FLOW CHART LATER##

The application should not need to know whether a heart-rate measurement came from a Polar watch, a smart ring, or OpenWear hardware.

# 🔒 Privacy Model

Privacy is a technical requirement of OpenWear, not merely a policy.

The initial design is:

#ADDFLOWCHARTLATER#

There is **no OpenWear server in the required data path**

**Privacy Principles**
