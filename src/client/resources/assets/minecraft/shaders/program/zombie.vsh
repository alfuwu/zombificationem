#version 150

in vec4 Position;

uniform mat4 ProjMat;
uniform vec2 OutSize;
uniform vec4 ColorModulate;

out vec2 texCoord;
out vec2 distortedCoord;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord = Position.xy / OutSize;

    // Add distortion
    vec2 distortion = vec2(sin(texCoord.y * 40.0), cos(texCoord.x * 30.0)) * 0.02;
    distortedCoord = texCoord + distortion;
}
