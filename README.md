# Disks Countdown timer

![Workflow result](https://github.com/AlexGabor/android-dev-challenge-compose-timer/workflows/Check/badge.svg)


## :scroll: Description
A countdown timer which uses 3 overlapping disks as user input (drag to rotate) to select the time. 
The disk are also animated to reflect the current time. 



## :bulb: Motivation and Context
The idea was to use an unconventional way to input the time which would also be a challenge to implement. 

The existing draggable modifier only works horizontally or vertically, so I needed to create one that works circularly.

A disk is somewhat reusable. It is implemented as a basic layout that places all children on it's edge spaced evenly.


## :camera_flash: Screenshots
<!-- You can add more screenshots here if you like -->
<img src="/results/screenshot_1.png" width="260">&emsp;<img src="/results/screenshot_2.png" width="260">

Tweet with video: https://twitter.com/AlexGabor42/status/1369345002623688704?s=20

## License
```
Copyright 2020 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```