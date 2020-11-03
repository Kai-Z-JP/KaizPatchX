#define RANGE 24
const int rng2 = RANGE - 1;
uniform sampler2D texture0, texture1;
uniform float width, height;
uniform float weight[RANGE];
uniform int pass;
uniform float threshold;

//ifは負荷高いので使わない
vec4 getColor(float x, float y){
	vec4 pos;
	vec4 depth;

	pos = vec4(x, y, gl_TexCoord[0].z, gl_TexCoord[0].w);
	depth = texture2DProj(texture1, pos);
	//pass0ではalphaは無視する
	return ((pass == 0) ? ((depth.g <= threshold) ? vec4(texture2DProj(texture0, pos).rgb, 1.0) : vec4(0.0)) : texture2DProj(texture0, pos));
}

void main(void){
	vec4  destColor = vec4(0.0);
	float wFrag = 1.0 / width;//pixel→uv
	float hFrag = 1.0 / height;//pixel→uv
	int i;
	float x, y;

	for(i = -rng2; i <= rng2; ++i)
	{
		x = gl_TexCoord[0].x + ((pass == 0) ? (float(i) * wFrag) : 0.0);//pass0:横ぼかし
		y = gl_TexCoord[0].y + ((pass == 1) ? (float(i) * hFrag) : 0.0);//pass1:縦ぼかし
		destColor += (getColor(x, y) * weight[abs(i)]);
	}
	//destColor = vec4(texture2DProj(texture1, gl_TexCoord[0]).rgb, 1.0);//深度描画(デバッグ用)
	//destColor = vec4(texture2DProj(texture0, gl_TexCoord[0]).rgb, 1.0);//通常描画(デバッグ用)

	gl_FragColor = destColor;
}
