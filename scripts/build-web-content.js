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
    '<link rel="stylesheet" href="/assets/site.css">'+
    '</head><body class="content"><main><div class="brand">BİLGİLİMEDIA</div><article class="card">'+
    '<div class="cat">'+esc(a.category)+' • '+esc(a.minutes)+' dk okuma</div>'+
    '<h1>'+esc(a.title)+'</h1>'+
    '<p class="summary">'+esc(a.summary)+'</p>'+image+
    '<div class="body">'+esc(a.body)+'</div>'+sources+
    '<p><a rel="noopener noreferrer" href="https://spoo.me/bilgilimedia">BilgiliMedia hesapları</a></p>'+
    '</article></main></body></html>';
  fs.writeFileSync(path.join(dir,"index.html"),html);
}
console.log("Static article pages:",feed.articles.length);
