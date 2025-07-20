#version 150

// Input from vertex shader
in vec2 texCoord;
in vec2 distortedCoord;
uniform sampler2D DiffuseSampler;

// Output to the framebuffer
out vec4 fragColor;

float effect(vec2 uv, float strength) {
    vec2 fromCenter = uv - 0.5;
    float radius = length(fromCenter);
    return smoothstep(0.8, 1.0, radius) * strength;
}

void main()
{
    // Sample the texture
    vec4 texColor = texture(DiffuseSampler, texCoord);

    // Desaturate the color
    vec3 desaturatedColor = vec3(dot(texColor.rgb, vec3(0.2126, 0.7152, 0.0722)));

    // Apply effect thingy
    float effectThing = effect(texCoord, 0.7);
    vec3 finalColor = desaturatedColor * (1.0 - effectThing);

    vec4 distortedColor = texture(DiffuseSampler, distortedCoord);

    // Mix distorted color with original color
    finalColor = mix(finalColor, distortedColor.rgb, 0.2);

    // Output final color
    fragColor = vec4(finalColor, texColor.a);
}