import { useCallback, useState, useRef } from 'react';
import Papa from 'papaparse';
import { Upload, FileSpreadsheet, CheckCircle2, X } from 'lucide-react';
import { useAppStore, type FileMetadata } from '@/store/useAppStore';
import { uploadCsv } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { cn } from '@/lib/utils';

export function UploadPage() {
  const { file, setFile, setActivePage } = useAppStore();
  const [isDragging, setIsDragging] = useState(false);
  const [progress, setProgress] = useState(0);
  const [uploading, setUploading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const processFile = useCallback((f: File) => {
    if (!f.name.endsWith('.csv')) return;
    setUploading(true);
    setProgress(0);

    // Simulate upload progress
    const interval = setInterval(() => {
      setProgress((p) => {
        if (p >= 90) { clearInterval(interval); return 90; }
        return p + Math.random() * 20;
      });
    }, 200);

    Papa.parse(f, {
      header: true,
      preview: 5,
      complete: async (results) => {
        clearInterval(interval);
        setProgress(100);

        try {
          await uploadCsv(f);

          const meta: FileMetadata = {
            name: f.name,
            size: f.size,
            rows: results.data.length,
            columns: results.meta.fields || [],
            preview: results.data as Record<string, string | number>[],
            uploadedAt: new Date(),
          };

          setTimeout(() => {
            setFile(meta);
            setUploading(false);
          }, 500);
        } catch (error) {
          clearInterval(interval);
          setUploading(false);
          console.error('CSV upload failed', error);
          alert(error instanceof Error ? error.message : 'CSV upload failed.');
        }
      },
      error: () => {
        clearInterval(interval);
        setUploading(false);
      },
    });
  }, [setFile]);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const f = e.dataTransfer.files[0];
    if (f) processFile(f);
  }, [processFile]);

  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (f) processFile(f);
  }, [processFile]);

  if (file && !uploading) {
    return (
      <div className="flex-1 p-4 md:p-8 animate-fade-in">
        <div className="max-w-4xl mx-auto">
          {/* Success Header */}
          <div className="glass rounded-xl p-6 mb-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-lg bg-success/10 flex items-center justify-center">
                  <CheckCircle2 className="h-5 w-5 text-success" />
                </div>
                <div>
                  <h3 className="font-semibold text-foreground">{file.name}</h3>
                  <p className="text-sm text-muted-foreground">
                    {(file.size / 1024).toFixed(1)} KB · {file.columns.length} columns · {file.rows} rows preview
                  </p>
                </div>
              </div>
              <div className="flex gap-2">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  onClick={() => setFile(null)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          {/* Preview Table */}
          <div className="glass rounded-xl overflow-hidden">
            <div className="px-5 py-3 border-b border-border">
              <h4 className="text-sm font-medium text-muted-foreground">Data Preview (First 5 rows)</h4>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border bg-muted/30">
                    {file.columns.map((col) => (
                      <th key={col} className="text-left px-4 py-3 font-medium text-muted-foreground whitespace-nowrap">
                        {col}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {file.preview.map((row, i) => (
                    <tr key={i} className="border-b border-border/50 hover:bg-muted/20 transition-colors">
                      {file.columns.map((col) => (
                        <td key={col} className="px-4 py-2.5 text-foreground whitespace-nowrap">
                          {String(row[col] ?? '')}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Action */}
          <div className="mt-6 flex justify-center">
            <Button onClick={() => setActivePage('chat')} className="px-6">
              Start Analyzing with AI →
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex items-center justify-center p-4 md:p-8">
      <div className="w-full max-w-xl animate-fade-in">
        <div className="text-center mb-8">
          <h1 className="text-2xl md:text-3xl font-bold text-foreground mb-2">Upload Your Data</h1>
          <p className="text-muted-foreground">Drop a CSV file to get started with AI-powered analytics</p>
        </div>

        <div
          onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          className={cn(
            'glass rounded-2xl border-2 border-dashed p-12 text-center cursor-pointer transition-all duration-200',
            isDragging
              ? 'border-primary bg-primary/5 scale-[1.02]'
              : 'border-border hover:border-primary/50 hover:bg-muted/30'
          )}
        >
          <input
            ref={inputRef}
            type="file"
            accept=".csv"
            className="hidden"
            onChange={handleFileChange}
          />

          {uploading ? (
            <div className="space-y-4">
              <FileSpreadsheet className="h-12 w-12 text-primary mx-auto animate-pulse-soft" />
              <div>
                <p className="font-medium text-foreground mb-1">Processing file...</p>
                <p className="text-sm text-muted-foreground mb-4">{Math.round(progress)}% complete</p>
                <Progress value={progress} className="h-1.5 max-w-xs mx-auto" />
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="h-16 w-16 rounded-2xl bg-primary/10 flex items-center justify-center mx-auto">
                <Upload className="h-7 w-7 text-primary" />
              </div>
              <div>
                <p className="font-medium text-foreground">
                  Drag & drop your CSV file here
                </p>
                <p className="text-sm text-muted-foreground mt-1">or click to browse</p>
              </div>
              <p className="text-xs text-muted-foreground/60">Supports .csv files up to 50MB</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
