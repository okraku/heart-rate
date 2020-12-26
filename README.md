# heart rate
This is an example for bidirectional communication between smartwatch and smartphone.

The smartwatch app displays the measured heart rate and sends it to connected devices (path: /heart_rate).
When the smartphone app receives the heart rate, it will display and evaluate it. If the heart rate
is too high, it will send a warning to the smartwatch (path: /heart_rate_warning). When the smartwatch
receives a warning, it will create a notification.
