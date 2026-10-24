const fs=require("fs");
const path=require("path");

const feed=JSON.parse(fs.readFileSync(path.join("hosting","feed.json"),"utf8"));
if(!feed || !Array.isArray(feed.articles)) throw new Error("Invalid feed");

const esc=(s)=>String(s??"").replace(/[&<>"']/g,(c)=>({
  "&":"&amp;","<":"&lt;",">":"&gt;","\"":"&quot;","'":"&#39;"
}[c]));

for(const a of feed.articles){
  const dir=path.join("hosting","content",String(a.id));
  fs.mkdirSync(dir,{recursive:true});
  const sources=Array.isArray(a.sources)&&a.sources.length
    ? '<h2>Kaynaklar</h2><ul>'+a.sources.map(s=>'<li><a rel="noopener noreferrer" href="'+esc(s.url)+'">'+esc(s.title||"Kaynak")+'</a></li>').join("")+'</ul>'
    : "";
  const image=a.imageUrl
    ? '<img class="cover" src="'+esc(a.imageUrl)+'" alt="'+esc(a.title)+'" loading="eager">'
    : "";
  const html='<!doctype html><html lang="tr"><head><meta charset="utf-8">'+
    '<meta name="viewport" content="width=device-width,initial-scale=1">'+
    '<title>'+esc(a.title)+' • BilgiliMedia</title>'+
    '<meta name="description" content="'+esc(a.summary||"BilgiliMedia içeriği")+'">'+
    '<style>*{box-sizing:border-box}body{font-family:system-ui,-apple-system,sans-serif;margin:0;background:#f7f8fb;color:#191b1f}main{max-width:760px;margin:0 auto;padding:28px 18px 56px}.brand{color:#e11f30;font-size:13px;font-weight:900;letter-spacing:.12em}.card{background:#fff;border-radius:22px;padding:22px;margin-top:16px;box-shadow:0 8px 30px rgba(0,0,0,.06)}.cat{color:#e11f30;font-size:12px;font-weight:800;text-transform:uppercase}h1{font-size:32px;line-height:1.12;margin:9px 0}.summary{font-size:17px;color:#626975;line-height:1.55}.body{font-size:17px;line-height:1.72;margin-top:24px}.cover{width:100%;border-radius:16px;margin:16px 0;max-height:420px;object-fit:cover}a{color:#e11f30;font-weight:700}li{margin:8px 0}</style>'+
    '</head><body><main><div class="brand">BİLGİLİMEDIA</div><article class="card">'+
    '<div class="cat">'+esc(a.category)+' • '+esc(a.minutes)+' dk okuma</div>'+
    '<h1>'+esc(a.title)+'</h1>'+
    '<p class="summary">'+esc(a.summary)+'</p>'+image+
    '<div class="body">'+esc(a.body)+'</div>'+sources+
    '<p><a href="https://spoo.me/bilgilimedia">BilgiliMedia hesapları</a></p>'+
    '</article></main></body></html>';
  fs.writeFileSync(path.join(dir,"index.html"),html);
}
console.log("Static article pages:",feed.articles.length);
